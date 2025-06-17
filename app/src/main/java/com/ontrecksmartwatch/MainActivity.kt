package com.ontrecksmartwatch

//noinspection SuspiciousImport
import NavigationStack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ontrecksmartwatch.theme.OnTrekSmartwatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            OnTrekSmartwatchTheme {
                NavigationStack(
//                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}