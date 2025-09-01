package com.ontrek.mobile.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.Wearable
import com.ontrek.mobile.R
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val FREQUENCY = 20
private const val SAMPLING_PERIODS = (1_000_000 / FREQUENCY)
private const val SLIDING = 70
private const val THRESHOLD = 0.60
private const val PROCEESS_VAR = 1e-5f
private const val MEASUREMENT_VAR = 1e-2f
private const val MODEL_NAME = "fall_model_coarse-gyro_lite.pt"
private const val WINDOW_SIZE = 140
private const val FEATURES = 6
private const val MAX_BUFFER_SIZE = WINDOW_SIZE * 2

data class TimedValue (
    val timestamp: Long,
    val value: FloatArray
)
class FallDetectionService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val alignedData = mutableListOf<FloatArray>()
    private val accelBuffer = mutableListOf<TimedValue>()
    private val gyroBuffer = mutableListOf<TimedValue>()
    private lateinit var module: Module
    inner class LocalBinder : Binder() {
        fun getService(): FallDetectionService = this@FallDetectionService
    }
    private val binder = LocalBinder()
    private var lastFallTime = 0L
    private var lastAccSampleTime = 0L
    private var lastGyroSampleTime = 0L

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

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SAMPLING_PERIODS
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SAMPLING_PERIODS
        )

        val notification = NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("Fall Detection")
            .setContentText("Monitoring for falls...")
            .setSmallIcon(R.drawable.hiking)
            .build()

        // Carica modello TorchScript dal folder assets
        module = LiteModuleLoader.load(loadModelFromFile())

        startForeground(1, notification)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val now = System.currentTimeMillis()
                if (now - lastAccSampleTime >= 25) {
                    accelBuffer.add(TimedValue(now, event.values.clone()))
                    lastAccSampleTime = now
                    if (gyroBuffer.isNotEmpty()) {
                        val closestGyro = gyroBuffer.minByOrNull { Math.abs(it.timestamp - now) }
                        if (closestGyro != null && Math.abs(closestGyro.timestamp - now) <= 12) {
                            alignedData.add(floatArrayOf(
                                event.values[0], event.values[1], event.values[2],
                                closestGyro.value[0], closestGyro.value[1], closestGyro.value[2]
                            ))
                        }
                    }
                }
            }
            Sensor.TYPE_GYROSCOPE -> {
                val now = System.currentTimeMillis()
                if (now - lastGyroSampleTime >= 25) {
                    gyroBuffer.add(TimedValue(now, event.values.clone()))
                    lastGyroSampleTime = now
                }
            }
        }

        if (alignedData.size >= WINDOW_SIZE) {
            val window = FloatArray(WINDOW_SIZE * FEATURES)
            for (j in 0 until FEATURES) {
                for (i in 0 until WINDOW_SIZE) {
                    window[j * WINDOW_SIZE + i] = alignedData[i][j]
                }
            }

            Log.d("FALL_DETECTION", "Window ready, first row: ${alignedData[0].joinToString(", ")}")


            elaborateData(applyKalman(window))
            alignedData.subList(0, SLIDING).clear()
        }
        if (accelBuffer.size > MAX_BUFFER_SIZE) {
            accelBuffer.subList(0, WINDOW_SIZE).clear()
        }
        if (gyroBuffer.size > MAX_BUFFER_SIZE) {
            gyroBuffer.subList(0, WINDOW_SIZE).clear()
        }
    }

    fun elaborateData(data: FloatArray) {
        val inputTensor = Tensor.fromBlob(
            data,
            longArrayOf(1, FEATURES.toLong(), WINDOW_SIZE.toLong())
        )

        try {
            val output = module.forward(IValue.from(inputTensor)).toTensor()
            val logits = output.dataAsFloatArray
            val probs = softmax(logits)
            val probabilityFall = probs[1]  // Indice 1 = caduta
            val probabilityNoFall = probs[0] // Indice 0 = non caduta

            val currentTime = System.currentTimeMillis()

            Log.d("FALL_DETECTION", "Probabilities -> No Fall: $probabilityNoFall, Fall: $probabilityFall")

            if (probabilityFall > THRESHOLD && (currentTime - lastFallTime > 30_000)) {
                sendResultToWatch()
                lastFallTime = currentTime
                Log.d("FALL_DETECTION", "Fall detected with probability: $probabilityFall")
                accelBuffer.clear()
                gyroBuffer.clear()
                alignedData.clear()
            }

        } catch (e: Exception) {
            Log.e("FALL_DETECTION", "Error during inference", e)
        }

    }

    fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exp = logits.map { Math.exp((it - maxLogit).toDouble()) }
        val sum = exp.sum()
        return exp.map { (it / sum).toFloat() }.toFloatArray()
    }

    // Applica filtro di Kalman a ciascuna colonna (feature)
    private fun applyKalman(data: FloatArray): FloatArray {
        val nRows = data.size / FEATURES   // numero di time step = windowSize
        val result = data.copyOf()

        // per ogni colonna (ax, ay, az, gx, gy, gz)
        for (col in 0 until FEATURES) {
            // estrai la sequenza temporale della colonna (es. ax0..axN)
            val columnValues = FloatArray(nRows) { row ->
                data[row * FEATURES + col]
            }

            // applica filtro Kalman alla colonna
            val filteredColumn = kalmanFilter1D(columnValues)

            // reinserisci nel risultato nello stesso layout row-major
            for (row in 0 until nRows) {
                result[row * FEATURES + col] = filteredColumn[row]
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

    private fun sendResultToWatch() {
        Log.d("FALL_DETECTION", "Sending fall alert to watch")
        val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(1f).array()

        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach { node ->
                    Wearable.getMessageClient(this).sendMessage(
                        node.id, "/fall_detection_result", bytes
                    )
                }
            }
    }

    private fun loadModelFromFile(): String {
        val file = File(filesDir, MODEL_NAME)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        assets.open(MODEL_NAME).use { inputStream ->
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

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
