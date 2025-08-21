package com.ontrek.mobile

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FallDetectionService : Service(), MessageClient.OnMessageReceivedListener {

    private lateinit var module: Module
    private val windowSize = 62  // per modello 50Hz, oppure 62 se 25Hz
    private val numFeatures = 6   // accel (x,y,z) + gyro (x,y,z)

    override fun onCreate() {
        super.onCreate()
        // Carica modello TorchScript dal folder assets
        module = Module.load(assetFilePath("fall_model_50hz.pt"))
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Qui ricevi finestre di dati raw dallo smartwatch
    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == "/fall_window") {
            val rawData = event.data // ByteArray con float accel+gyro
            val floatData = byteArrayToFloatArray(rawData)

            // 1. Applica filtro di Kalman
            val filteredData = applyKalman(floatData)

            // 2. Crea tensore Torch (shape [1, windowSize, numFeatures])
            val inputTensor = Tensor.fromBlob(
                filteredData,
                longArrayOf(1, windowSize.toLong(), numFeatures.toLong())
            )

            // 3. Inference
            val output = module.forward(IValue.from(inputTensor)).toTensor()
            val result = output.dataAsFloatArray

            Log.d("FALL_DETECTION", "Output ML: ${result.joinToString()}")

            // 4. Invia risultato allo smartwatch
            sendResultToWatch(result)
        }
    }

    private fun byteArrayToFloatArray(bytes: ByteArray): FloatArray {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floats = FloatArray(bytes.size / 4)
        buffer.asFloatBuffer().get(floats)
        return floats
    }

    // Applica filtro Kalman (semplificato, traduzione dal Python che usavi)
    private fun applyKalman(data: FloatArray): FloatArray {
        val n = data.size / numFeatures
        val result = data.copyOf()

        for (col in 0 until numFeatures) {
            val column = FloatArray(n) { i -> data[i * numFeatures + col] }
            val filtered = kalmanFilter1D(column)
            for (i in 0 until n) {
                result[i * numFeatures + col] = filtered[i]
            }
        }
        return result
    }

    private fun kalmanFilter1D(data: FloatArray,
                               processVar: Float = 1e-5f,
                               measurementVar: Float = 1e-2f): FloatArray {
        val n = data.size
        val xEst = FloatArray(n)
        val P = FloatArray(n)

        val Q = processVar
        val R = measurementVar

        xEst[0] = data[0]
        P[0] = 1f

        for (k in 1 until n) {
            val xPred = xEst[k - 1]
            val PPred = P[k - 1] + Q

            val K = PPred / (PPred + R)
            xEst[k] = xPred + K * (data[k] - xPred)
            P[k] = (1 - K) * PPred
        }
        return xEst
    }

    private fun sendResultToWatch(result: FloatArray) {
        val resultStr = if (result[0] > 0.5f) "FALL" else "NO_FALL"
        val bytes = resultStr.toByteArray()

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this).sendMessage(
                        node.id, "/fall_detection_result", bytes
                    )
                }
            }
    }

    // Utility per caricare file TorchScript da assets
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
