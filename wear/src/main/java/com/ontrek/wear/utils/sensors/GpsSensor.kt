package com.ontrek.wear.utils.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GpsSensor(val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

    private val LOCATION_REFRESH_TIME = 1000L // Update interval in milliseconds
    private val LOCATION_REFRESH_DISTANCE = 1f // Update distance in meters

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _accuracy = MutableStateFlow(0f)
    val accuracy: StateFlow<Float> = _accuracy.asStateFlow()

    private val locationCallback = LocationListener { locationResult ->
        locationResult.let { location ->
            _location.value = location
            _accuracy.value = location.accuracy
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_REFRESH_TIME)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(LOCATION_REFRESH_TIME)
//                .setMinUpdateDistanceMeters(LOCATION_REFRESH_DISTANCE)
                .build()

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        )
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}