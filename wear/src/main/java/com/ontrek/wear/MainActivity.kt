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
import androidx.lifecycle.viewmodel.compose.viewModel
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
    private val preferencesViewModel : PreferencesViewModel by viewModels { PreferencesViewModel.Factory }  // TODO: CHIEDERE A DECO PERCHÉ CE NE SONO DUE

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setTheme(Theme_DeviceDefault)

        setContent {
            val preferencesViewModel : PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory)  // TODO: CHIEDERE A DECO PERCHÉ CE NE SONO DUE
            val token by preferencesViewModel.tokenState.collectAsState()
            OnTrekTheme {
                // At startup the token is "undefined" because it has not been fetched yet
                // TODO: distingush between loading token and undefined token
//                if (token.isNullOrEmpty()) {
//                    Loading(Modifier.fillMaxSize())
//                } else
                    if (token.isNullOrEmpty()) {
                        Login(Modifier.fillMaxSize())
                    } else
                    NavigationStack()
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