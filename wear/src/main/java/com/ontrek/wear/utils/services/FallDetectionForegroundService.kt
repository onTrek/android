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
private const val test = false
private const val testSet = "test_dataset_${freq}Hz-vga.json"

data class MockItem(
    val sequence: List<List<Double>>,
    val label: Int
)

data class TimedValues(
    val time: Long,
    val values: FloatArray
)

class FallDetectionForegroundService : Service(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private val accelBuffer = mutableListOf<TimedValues>()
    private val gyroBuffer = mutableListOf<TimedValues>()
    private val alignedData = mutableListOf<FloatArray>()
    private var latestQuaternion = FloatArray(4) // [w, x, y, z]

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
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
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
        accelBuffer.clear()
        gyroBuffer.clear()
        alignedData.clear()
        latestQuaternion = FloatArray(4) { 0f }
        mockData = null
        Log.d("FALL_DETECTION", "Service destroyed")
    }

    private fun getWorldAcceleration(acc: FloatArray, quat: FloatArray): FloatArray {
        val w = quat[0]
        val x = quat[1]
        val y = quat[2]
        val z = quat[3]

        val ax = acc[0]
        val ay = acc[1]
        val az = acc[2]

        // Matrice di rotazione 3x3 dal quaternione
        val r00 = 1 - 2*(y*y + z*z)
        val r01 = 2*(x*y - z*w)
        val r02 = 2*(x*z + y*w)

        val r10 = 2*(x*y + z*w)
        val r11 = 1 - 2*(x*x + z*z)
        val r12 = 2*(y*z - x*w)

        val r20 = 2*(x*z - y*w)
        val r21 = 2*(y*z + x*w)
        val r22 = 1 - 2*(x*x + y*y)

        // Trasformiamo l'accelerazione dal sistema device → mondo
        val worldAx = r00 * ax + r01 * ay + r02 * az
        val worldAy = r10 * ax + r11 * ay + r12 * az
        val worldAz = r20 * ax + r21 * ay + r22 * az

        return floatArrayOf(worldAx, worldAy, worldAz)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val acc = getWorldAcceleration(event.values.clone(), latestQuaternion)
                accelBuffer.add(TimedValues(event.timestamp, acc))

                if (gyroBuffer.isNotEmpty()) {
                    val accTime = event.timestamp
                    val gyro = gyroBuffer.minByOrNull { g -> kotlin.math.abs(g.time - accTime) }
                    if (gyro != null) {
                        val sample = floatArrayOf(
                            acc[0], acc[1], acc[2],
                            gyro.values[0], gyro.values[1], gyro.values[2]
                        )
                        alignedData.add(sample)
                    }
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroBuffer.add(TimedValues(event.timestamp, event.values.clone()))
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                val quat = FloatArray(4)
                SensorManager.getQuaternionFromVector(quat, event.values)
                latestQuaternion = quat
            }
        }

        var testWindow = FloatArray(0)
        var item = MockItem(listOf(), 0)

        if (alignedData.size >= windowSize) {
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
                val sample = alignedData[i]
                window[i * 6 + 0] = sample[0] // worldAx
                window[i * 6 + 1] = sample[1] // worldAy
                window[i * 6 + 2] = sample[2] // worldAz
                window[i * 6 + 3] = sample[3] // gx
                window[i * 6 + 4] = sample[4] // gy
                window[i * 6 + 5] = sample[5] // gz
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

            alignedData.subList(0, sliding).clear()
            val maxBufferSize = windowSize * 2
            if (accelBuffer.size > maxBufferSize) {
                accelBuffer.subList(0, accelBuffer.size - maxBufferSize).clear()
            }
            if (gyroBuffer.size > maxBufferSize) {
                gyroBuffer.subList(0, gyroBuffer.size - maxBufferSize).clear()
            }
        }

    }

    fun elaborateResponse(event: MessageEvent, onFallDetected: () -> Unit) {
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
            onFallDetected()
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
