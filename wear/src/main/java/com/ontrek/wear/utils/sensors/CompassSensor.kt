package com.ontrek.wear.utils.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CompassSensor(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _direction = MutableStateFlow(0f)
    val direction: StateFlow<Float> = _direction

    private val _accuracy = MutableStateFlow(3)
    val accuracy: StateFlow<Int> = _accuracy
    private val _vibrationNeeded = MutableStateFlow(false)
    val vibrationNeeded: StateFlow<Boolean> = _vibrationNeeded

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, gravity, 0, 3)
                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            }

            val rotation = FloatArray(9)
            if (SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotation, orientation)
                val azimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
                _direction.value = azimuth
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Log.d("COMPASS", "Sensor accuracy changed: ${sensor.name} - $accuracy")
            if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                _accuracy.value = accuracy
            }
        }
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun setVibrationNeeded(isNeeded: Boolean) {
        Log.d("COMPASS", "Vibration needed: $isNeeded")
        _vibrationNeeded.value = isNeeded
    }

    fun stop() {
        sensorManager.unregisterListener(sensorListener)
        _vibrationNeeded.value = false
    }
}