package com.ontrek.wear

import android.Manifest
import android.R.style.Theme_DeviceDefault
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material3.AppScaffold
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.wear.data.PreferencesViewModel
import com.ontrek.wear.screens.NavigationStack
import com.ontrek.wear.screens.login.Login
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.components.PermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val preferencesViewModel: PreferencesViewModel by viewModels { PreferencesViewModel.Factory }
    private var hasPermissions = false
    private lateinit var ambientController: AmbientLifecycleObserver
    val isInAmbientMode = MutableStateFlow(false)
    private var ambientModeEnabled by mutableStateOf(false)

    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Permesso di localizzazione precisa concesso
                Log.d("GPS_PERMISSIONS", "Permesso di localizzazione precisa concesso")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Solo permesso di localizzazione approssimativa concesso
                Log.d("GPS_PERMISSIONS", "Solo permesso di localizzazione approssimativa concesso")
            }

            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {
                // Permesso di notifiche concesso
                Log.d("GPS_PERMISSIONS", "Permesso di notifiche concesso")
            }

            else -> {
                // Nessun permesso concesso
                Log.d("GPS_PERMISSIONS", "Permessi di localizzazione negati")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setTheme(Theme_DeviceDefault)

        ambientController = AmbientLifecycleObserver(this, AmbientCallback())
        lifecycle.addObserver(ambientController)

        val context = this
        val localPermissions = checkAndRequestPermissions()
        setContent {
            OnTrekTheme {
                val token by preferencesViewModel.tokenState.collectAsState()
                RetrofitClient.initialize(preferencesViewModel)
                Log.d("WATCH_CONNECTION", "Token state: \"$token\"")
                when {
                    token == null -> Loading(Modifier.fillMaxSize())
                    // if GPS permissions are not granted, show a message or handle it
                    (!localPermissions) -> {
                        PermissionRequester(context, ambientModeEnabled)
                    }

                    token!!.isEmpty() -> Login(
                        preferencesViewModel::saveToken,
                        preferencesViewModel::saveCurrentUser,
                    )
                    else -> AppScaffold {
                        NavigationStack()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(ambientController)
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)

        val newPermissionState = checkPermissions()

        // Se lo stato dei permessi è cambiato, riavvia l'activity per aggiornare l'UI
        if (hasPermissions != newPermissionState) {
            hasPermissions = newPermissionState
            recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/auth"
            ) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                preferencesViewModel.saveToken(dataMap.getString("token") ?: "")
                preferencesViewModel.saveCurrentUser(dataMap.getString("currentUser") ?: "")
            }
        }
    }

    fun checkPermissions(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        try {
            ambientModeEnabled =
                Settings.Global.getInt(contentResolver, "ambient_enabled") == 1
        } catch (e: Exception) {
            Log.e("AMBIENT_MODE", "Error checking ambient mode setting: ${e.message}")
        }

        Log.d("PERMISSIONS", "Fine Location: $hasFineLocationPermission, Coarse Location: $hasCoarseLocationPermission, Notifications: $hasNotificationPermission, Ambient Mode: $ambientModeEnabled")

        return hasFineLocationPermission && hasCoarseLocationPermission && hasNotificationPermission && ambientModeEnabled
    }

    private fun checkAndRequestPermissions(): Boolean {
        if (!checkPermissions()) {
            permissionsRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            hasPermissions = true
            return true
        }
        return false
    }

    private inner class AmbientCallback : AmbientLifecycleObserver.AmbientLifecycleCallback {
        override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
            isInAmbientMode.value = true
            val layoutParams = window.attributes
            layoutParams.screenBrightness = 0.0f
            window.attributes = layoutParams

        }

        override fun onExitAmbient() {
            isInAmbientMode.value = false
            val layoutParams = window.attributes
            layoutParams.screenBrightness = -1.0f //system default brightness
            window.attributes = layoutParams
        }
    }
}