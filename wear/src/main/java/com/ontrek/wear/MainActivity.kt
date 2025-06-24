package com.ontrek.wear

import NavigationStack
import android.R.style.Theme_DeviceDefault
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.ontrek.wear.data.PreferencesViewModel
import com.ontrek.wear.screens.login.Login
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val preferencesViewModel : PreferencesViewModel by viewModels { PreferencesViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setTheme(Theme_DeviceDefault)

        setContent {
            val token by preferencesViewModel.tokenState.collectAsState()
            OnTrekTheme {
                Log.d("WATCH_CONNECTION", "Token state: \"$token\"")
                when {
                        token == null -> Loading(Modifier.fillMaxSize())
                        token!!.isEmpty() -> Login()
                        else -> NavigationStack()
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("WATCH_CONNECTION", "Resuming activity, registering data listener")
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("WATCH_CONNECTION", "Querying data changes")

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED &&
                event.dataItem.uri.path == "/auth") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                preferencesViewModel.saveToken(dataMap.getString("token") ?: "")
            }
        }
    }
}