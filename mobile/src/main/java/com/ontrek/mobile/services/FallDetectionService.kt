package com.ontrek.mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.pow

private const val THRESHOLD = 0.9
private const val VARIANCE_THRESHOLD = 0.01
private const val ACCELL_THRESHOLD = 2.5f
private const val PROCEESS_VAR = 1e-5f
private const val MEASUREMENT_VAR = 1e-2f
private const val MODEL_NAME = "fall_model_cicb_50hz_lite.pt"


class FallDetectionService : Service(), MessageClient.OnMessageReceivedListener {

    private lateinit var module: Module
    private val windowSize = 125  // 125 per modello 50Hz, oppure 62 se 25Hz
    private val numFeatures = 6   // accel (x,y,z) + gyro (x,y,z)
    private var lastFallTime = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d("FALL_DETECTION", "Service created")

        val channel = NotificationChannel(
            "fall_channel",
            "Fall Detection Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Channel for fall detection service"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("Fall Detection")
            .setContentText("Monitoring for falls...")
            .setSmallIcon(R.drawable.hiking)
            .build()

        // Carica modello TorchScript dal folder assets
        module = LiteModuleLoader.load(assetFilePath(MODEL_NAME))

        Wearable.getMessageClient(this).addListener(this)

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floats = FloatArray(bytes.size / 4)
        buffer.asFloatBuffer().get(floats)
        return floats
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d("FALL_DETECTION", "Message received on path: ${event.path}")

        if (event.path == "/fall_window") {
            val rawData = event.data

            // 1. Converte ByteArray -> FloatArray
            val floatData = byteArrayToFloatArray(rawData)

            // 2. Filtro di Kalman
            val filteredData = applyKalman(floatData)

            // 2a. Controllo picchi accelerometro > 2.5g
            val accelPeaks = (0 until filteredData.size step 6)
                .any { i ->
                    val ax = filteredData[i]
                    val ay = filteredData[i + 1]
                    val az = filteredData[i + 2]
                    ax > ACCELL_THRESHOLD || ay > ACCELL_THRESHOLD || az > ACCELL_THRESHOLD
                }

            // 2b. Controllo inattività (solo per log/monitoraggio)
            val accelValues = (0 until filteredData.size step 6).flatMap { i ->
                listOf(filteredData[i], filteredData[i + 1], filteredData[i + 2])
            }
            val mean = accelValues.average()
            val variance = accelValues.map { (it - mean).pow(2) }.average()
            val inactive = variance < VARIANCE_THRESHOLD

            // 3. Crea tensore Torch
            val inputTensor = Tensor.fromBlob(
                filteredData,
                longArrayOf(1, windowSize.toLong(), numFeatures.toLong())
            )

            // 4. Inference
            try {
                val output = module.forward(IValue.from(inputTensor)).toTensor()
                val probabilityFall = getFallProbability(output)

                val currentTime = System.currentTimeMillis()
                // Invia caduta se supera la soglia e timeout, indipendentemente dall'inattività
                if (probabilityFall > THRESHOLD && accelPeaks && (currentTime - lastFallTime) > 5000) {
                    sendResultToWatch(1f)
                    lastFallTime = currentTime
                    Log.d("FALL_DETECTION", "Fall detected with probability: $probabilityFall")

                    if (inactive) {
                        Log.d("FALL_DETECTION", "User is inactive post-fall")
                        // Qui puoi aggiungere allarmi o monitoraggio post-caduta
                    }
                }

            } catch (e: Exception) {
                Log.e("FALL_DETECTION", "Error during inference", e)
            }
        }
    }


    fun getFallProbability(outputTensor: Tensor): Double {
        val logits = outputTensor.dataAsFloatArray
        if (logits.size != 2) {
            throw IllegalArgumentException("Expected 2-class output, got ${logits.size} elements")
        }

        val exp0 = Math.exp(logits[0].toDouble())
        val exp1 = Math.exp(logits[1].toDouble())
        val sumExp = exp0 + exp1

        return exp1 / sumExp  // probabilità della classe "caduta"
    }



    // Applica filtro di Kalman a ciascuna colonna (feature)
    private fun applyKalman(data: FloatArray): FloatArray {
        val nRows = data.size / numFeatures   // numero di time step = windowSize
        val result = data.copyOf()

        // per ogni colonna (ax, ay, az, gx, gy, gz)
        for (col in 0 until numFeatures) {
            // estrai la sequenza temporale della colonna (es. ax0..axN)
            val columnValues = FloatArray(nRows) { row ->
                data[row * numFeatures + col]
            }

            // applica filtro Kalman alla colonna
            val filteredColumn = kalmanFilter1D(columnValues)

            // reinserisci nel risultato nello stesso layout row-major
            for (row in 0 until nRows) {
                result[row * numFeatures + col] = filteredColumn[row]
            }
        }
        return result
    }

    // Filtro Kalman 1D (semplificato)
    private fun kalmanFilter1D(
        data: FloatArray,
        processVar: Float = PROCEESS_VAR,
        measurementVar: Float = MEASUREMENT_VAR
    ): FloatArray {
        val n = data.size
        val xEst = FloatArray(n)  // stima
        val P = FloatArray(n)     // covarianza errore

        val Q = processVar        // rumore di processo
        val R = measurementVar    // rumore di misura

        // inizializzazione
        xEst[0] = data[0]
        P[0] = 1f

        // ciclo Kalman
        for (k in 1 until n) {
            // Predizione
            val xPred = xEst[k - 1]
            val PPred = P[k - 1] + Q

            // Aggiornamento
            val K = PPred / (PPred + R)
            xEst[k] = xPred + K * (data[k] - xPred)
            P[k] = (1 - K) * PPred
        }

        return xEst
    }

    private fun sendResultToWatch(result: Float) {
        Log.d("FALL_DETECTION", "Sending result to watch: $result")
        val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(result).array()

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this).sendMessage(
                        node.id, "/fall_detection_result", bytes
                    )
                }
            }
    }

    private fun assetFilePath(assetName: String): String {
        val file = File(filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            return file.absolutePath
        }
    }
}
