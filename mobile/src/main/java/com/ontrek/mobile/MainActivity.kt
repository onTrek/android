package com.ontrek.mobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.NavigationStack
import com.ontrek.mobile.screens.auth.AuthScreen
import com.ontrek.mobile.ui.theme.OnTrekTheme

class MainActivity : ComponentActivity(){
    private val preferencesViewModel: PreferencesViewModel by viewModels { PreferencesViewModel.Factory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            OnTrekTheme {
                val token by preferencesViewModel.tokenState.collectAsState()
                when {
                    token == null -> CircularProgressIndicator()
                    token!!.isEmpty() -> AuthScreen()
                    else -> NavigationStack()
                    }
            }
        }

    }

    fun login(context: Context) {
        val putDataMapReq = PutDataMapRequest.create("/auth").apply {
            dataMap.putString("token", "3e530eef-2f77-418e-89e7-d82537c9109a")
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val request = putDataMapReq.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener { Log.d("WATCH_CONNECTION", "Started Activity") }
            .addOnFailureListener { Log.e("WATCH_CONNECTION", "Failed to send data", it) }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OnTrekTheme {
        Greeting("Android")
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