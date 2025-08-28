package com.ontrek.wear.utils.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ontrek.wear.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val freq = 50
private const val samplingPeriodUs = (1_000_000 / freq) // in microseconds
private const val windowSize = (freq * 2.5).toInt() // 62 samples for 2.5 seconds at 25Hz, 125 for 2.5 seconds at 50Hz
private const val sliding = 62
private const val test = true
private const val testSet = "test_dataset${freq}Hz.json"

data class MockItem(
    val sequence: List<List<Double>>,
    val label: Int
)

class FallDetectionForegroundService : Service(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private val accelData = mutableListOf<FloatArray>()
    private val gyroData = mutableListOf<FloatArray>()
    private var mockData = null as List<MockItem>?

    private var stopSending = false
    private var stopSendingTime = 0L

    inner class LocalBinder : Binder() {
        fun getService(): FallDetectionForegroundService = this@FallDetectionForegroundService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

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

        if (test) {
            Thread {
                mockData = loadMockData()
                Log.d("FALL_DETECTION", "Mock data loaded with ${mockData?.size} items")
            }.start()
        } else {
            mockData = null
        }

        Log.d("FallService", "Service created")

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            samplingPeriodUs
        )
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            samplingPeriodUs
        )

        val notification = NotificationCompat.Builder(this, "fall_channel")
            .setContentTitle("Fall Detection")
            .setContentText("Monitoring for falls...")
            .setSmallIcon(R.drawable.hiking)
            .build()

        startForeground(1, notification)
    }

    fun Context.loadMockData(): List<MockItem> {
        val json = readJsonFromAssets(testSet)
        val type = object : TypeToken<List<MockItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun Context.readJsonFromAssets(fileName: String): String {
        return assets.open(fileName).bufferedReader().use { it.readText() }
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelData.add(event.values.clone())
            Sensor.TYPE_GYROSCOPE -> gyroData.add(event.values.clone())
        }

        var testWindow = FloatArray(0)
        var item = MockItem(listOf(), 0)

        if (accelData.size >= windowSize && gyroData.size >= windowSize) {
            if (test) {
                item = mockData?.random() ?: return
                testWindow = FloatArray(item.sequence.size * 6)
                for (i in item.sequence.indices) {
                    testWindow[i * 6 + 0] = item.sequence[i][0].toFloat()
                    testWindow[i * 6 + 1] = item.sequence[i][1].toFloat()
                    testWindow[i * 6 + 2] = item.sequence[i][2].toFloat()
                    testWindow[i * 6 + 3] = item.sequence[i][3].toFloat()
                    testWindow[i * 6 + 4] = item.sequence[i][4].toFloat()
                    testWindow[i * 6 + 5] = item.sequence[i][5].toFloat()
                }
            }

            val window = FloatArray(windowSize * 6)
            for (i in 0 until windowSize) {
                window[i * 6 + 0] = accelData[i][0]
                window[i * 6 + 1] = accelData[i][1]
                window[i * 6 + 2] = accelData[i][2]
                window[i * 6 + 3] = gyroData[i][0]
                window[i * 6 + 4] = gyroData[i][1]
                window[i * 6 + 5] = gyroData[i][2]
            }

            if (stopSending && System.currentTimeMillis() - stopSendingTime > 5000) {
                stopSending = false
            } else if (!stopSending) {
                if (test) {
                    Log.d("FALL_DETECTION", "Sending window of size: ${testWindow.size}")
                    Log.d("FALL_DETECTION", "First 6 values: ${testWindow.take(6)}")
                    Log.d("FALL_RESULT", "True label: ${item.label}")
                    sendWindowToPhone(testWindow)
                } else {
                    Log.d("FALL_DETECTION", "Sending window of size: ${window.size}")
                    Log.d("FALL_DETECTION", "First 6 values: ${window.take(6)}")
                    sendWindowToPhone(window)
                }
            }

            accelData.subList(0, sliding).clear()
            gyroData.subList(0, sliding).clear()
        }

    }

    fun elaborateResponse(event: MessageEvent, function: () -> Unit) {
        val bytes = event.data
        if (bytes.size != 4) {
            Log.e("FALL_RESULT", "Invalid data size: ${bytes.size}, expected 4")
            return
        }

        // Ricostruisci il singolo float
        val result = ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .float

        Log.d("FALL_RESULT", "Fall probability: $result")

        // Logica di rilevamento caduta
        if (result == 1f) {
            Log.d("FALL_RESULT", "Fall detected!")
            function()
            stopSending = true
            stopSendingTime = System.currentTimeMillis()
        } else {
            Log.d("FALL_RESULT", "No fall")
        }
    }

    private fun sendWindowToPhone(window: FloatArray) {
        val nodeClient = Wearable.getNodeClient(this)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            val nodeId = nodes.firstOrNull()?.id
            if (nodeId == null) {
                Log.w("FALL_DETECTION", "Nodes not found")
                return@addOnSuccessListener
            }

            val byteBuffer = ByteBuffer.allocate(window.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            window.forEach { byteBuffer.putFloat(it) }

            Wearable.getMessageClient(this)
                .sendMessage(nodeId, "/fall_window", byteBuffer.array())
                .addOnSuccessListener {
                    Log.d("FALL_DETECTION", "Successfully sent window to phone with nodeId: $nodeId" )
                }
                .addOnFailureListener { e ->
                    Log.e("FALL_DETECTION", "Error sending message", e)
                }
        }.addOnFailureListener { e ->
            Log.e("FALL_DETECTION", "Error getting connected nodes", e)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
