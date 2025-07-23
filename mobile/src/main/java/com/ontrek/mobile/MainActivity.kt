package com.ontrek.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.NavigationStack
import com.ontrek.mobile.screens.auth.AuthScreen
import com.ontrek.mobile.ui.theme.OnTrekTheme
import com.ontrek.shared.api.RetrofitClient

class MainActivity : ComponentActivity(){
    private val preferencesViewModel: PreferencesViewModel by viewModels { PreferencesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            OnTrekTheme {
                val token by preferencesViewModel.tokenState.collectAsState()
                // Inizializza l'API client con il token corrente
                RetrofitClient.initialize(preferencesViewModel)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        token == null -> CircularProgressIndicator()
                        token!!.isEmpty() -> AuthScreen()
                        else -> NavigationStack()
                    }
                }
            }
        }

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