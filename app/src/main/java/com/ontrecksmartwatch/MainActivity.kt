package com.ontrecksmartwatch

//noinspection SuspiciousImport
import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ontrecksmartwatch.screens.home.HomeScreen
import com.ontrecksmartwatch.theme.OnTrekSmartwatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_DeviceDefault)

        setContent {
            OnTrekSmartwatchTheme {
                HomeScreen()
            }
        }
    }
}