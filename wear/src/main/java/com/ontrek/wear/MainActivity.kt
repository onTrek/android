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
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.ontrek.shared.api.RetrofitClient
import com.ontrek.wear.data.PreferencesViewModel
import com.ontrek.wear.screens.NavigationStack
import com.ontrek.wear.screens.login.Login
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.components.PermissionRequester
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener {

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
                Log.d("GPS_PERMISSIONS", "Permesso di localizzazione precisa concesso")
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("GPS_PERMISSIONS", "Solo permesso di localizzazione approssimativa concesso")
            }

            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {
                Log.d("GPS_PERMISSIONS", "Permesso di notifiche concesso")
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false) -> {
                Log.d("BT_PERMISSIONS", "Permesso BLUETOOTH_CONNECT concesso")
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) -> {
                Log.d("BT_PERMISSIONS", "Permesso BLUETOOTH_SCAN concesso")
            }

            else -> {
                Log.d("PERMISSIONS", "Alcuni permessi richiesti non sono stati concessi")
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

        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onMessageReceived(event: MessageEvent) {
        Log.d("FALL_DETECTION", "Message received on path: ${event.path}")

        if (event.path == "/fall_detection_result") {
            val bytes = event.data
            if (bytes.size % 4 != 0) {
                Log.e("FALL_RESULT", "Invalid float array size: ${bytes.size}")
                return
            }

            // Ricostruisci i float dall'array di byte
            val floatArray = FloatArray(bytes.size / 4)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floatArray)

            Log.d("FALL_RESULT", "Result floats: ${floatArray.joinToString()}")

            // Esempio: se consideri caduta se probabilità > 0.5
            val probabilityFall = floatArray[0]
            if (probabilityFall > 0.5) {
                Log.d("FALL_RESULT", "Fall detected!")
                // TODO: azioni in caso di caduta

            } else {
                Log.d("FALL_RESULT", "No fall")
            }
        }
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
        val neededPermissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        if (neededPermissions.isNotEmpty()) {
            permissionsRequest.launch(neededPermissions.toTypedArray())
            return false
        }

        hasPermissions = true
        return true
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