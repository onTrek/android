package com.ontrek.wear.utils.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.Wearable
import com.ontrek.wear.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val freq = 25
private const val samplingPeriodUs = (1_000_000 / freq) // in microseconds
private const val windowSize = (freq * 2.5).toInt() // 62 samples for 2.5 seconds at 25Hz, 125 for 2.5 seconds at 50Hz
private const val sliding = (freq * 2)

class FallDetectionForegroundService : Service(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private val accelData = mutableListOf<FloatArray>()
    private val gyroData = mutableListOf<FloatArray>()

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

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> accelData.add(event.values.clone())
            Sensor.TYPE_GYROSCOPE -> gyroData.add(event.values.clone())
        }

        if (accelData.size >= windowSize && gyroData.size >= windowSize) {
            val window = FloatArray(windowSize * 6)
            for (i in 0 until windowSize) {
                window[i * 6 + 0] = accelData[i][0]
                window[i * 6 + 1] = accelData[i][1]
                window[i * 6 + 2] = accelData[i][2]
                window[i * 6 + 3] = gyroData[i][0]
                window[i * 6 + 4] = gyroData[i][1]
                window[i * 6 + 5] = gyroData[i][2]
            }

            Log.d("FALL_DETECTION", "Sending window of size: ${window.size}")
            Log.d("FALL_DETECTION", "First 6 values: ${window.take(6)}")
            sendWindowToPhone(window)


            // Sliding window: rimuove i primi 25 campioni
            accelData.subList(0, sliding).clear()
            gyroData.subList(0, sliding).clear()
        }
    }

    private fun sendWindowToPhone(window: FloatArray) {
        Log.d("FALL_DETECTION", "Preparing to send window to phone")

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
