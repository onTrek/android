package com.ontrek.mobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.NavigationStack
import com.ontrek.mobile.screens.auth.AuthScreen
import com.ontrek.mobile.services.FallDetectionService
import com.ontrek.mobile.ui.theme.OnTrekTheme
import com.ontrek.mobile.utils.components.PermissionRequester
import com.ontrek.shared.api.RetrofitClient

class MainActivity : ComponentActivity() {

    private val preferencesViewModel: PreferencesViewModel by viewModels { PreferencesViewModel.Factory }
    private var hasPermissions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val localPermissions = checkAndRequestPermissions() // controlla e richiede permission se necessario

        setContent {
            OnTrekTheme {
                val token by preferencesViewModel.tokenState.collectAsState()
                RetrofitClient.initialize(preferencesViewModel)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        token == null -> CircularProgressIndicator()
                        !localPermissions -> PermissionRequester(this)
                        token!!.isEmpty() -> AuthScreen()
                        else -> NavigationStack()
                    }
                }
            }
        }

        if (localPermissions) {
            startFallDetectionService()
        }
    }

    private val permissionsRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false) -> {
                Log.d("NOTIFICATION_PERMISSIONS", "Notification permission granted")
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false) -> {
                Log.d("BT_PERMISSIONS", "BLUETOOTH_CONNECT permission granted")
            }

            permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false) -> {
                Log.d("BT_PERMISSIONS", "BLUETOOTH_SCAN permission granted")
            }

            else -> {
                Log.d("PERMISSIONS", "Some permissions were denied")
            }
        }
    }

    // Controlla le permission necessarie e le richiede se non presenti
    private fun checkAndRequestPermissions(): Boolean {
        val neededPermissions = mutableListOf<String>()

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

    fun checkPermissions(): Boolean {
        val hasNotificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        val hasBluetoothConnectPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        val hasBluetoothScanPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("PERMISSIONS", "Notifications: $hasNotificationPermission, Bluetooth Connect: $hasBluetoothConnectPermission, Bluetooth Scan: $hasBluetoothScanPermission")

        return hasNotificationPermission && hasBluetoothConnectPermission && hasBluetoothScanPermission
    }

    override fun onResume() {
        super.onResume()

        val newPermissionState = checkPermissions()

        // Se lo stato dei permessi è cambiato, riavvia l'activity per aggiornare l'UI
        if (hasPermissions != newPermissionState) {
            hasPermissions = newPermissionState
            recreate()
            if (hasPermissions) {
                startFallDetectionService()
            }
        }

    }

    // Avvia il servizio in foreground
    private fun startFallDetectionService() {
        val intent = Intent(this, FallDetectionService::class.java)
        startForegroundService(intent)
    }
}
/* Può tornare utile per debuggare la connessione tra mobile e watch
fun printSignatureSHA1(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(
        context.packageName,
        PackageManager.GET_SIGNING_CERTIFICATES // Use GET_SIGNATURES on old APIs
    )

    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo.signingInfo?.apkContentsSigners
    } else {
        @Suppress("DEPRECATION")
        packageInfo.signatures
    }

    if (signatures != null) {
        for (signature in signatures) {
            val digest = MessageDigest.getInstance("SHA1")
            digest.update(signature.toByteArray())
            val sha1 = digest.digest().joinToString(":") {
                String.format("%02X", it)
            }
            Log.d("AppSignature", "SHA-1: $sha1")
        }
    }
}*/