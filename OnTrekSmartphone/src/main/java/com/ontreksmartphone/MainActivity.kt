package com.ontreksmartphone

import NavigationStack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
//import com.ontrecksmartwatch.theme.OnTrekSmartwatchTheme
import com.ontreksmartphone.ui.theme.OnTrekSmartwatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnTrekSmartwatchTheme {
                NavigationStack()
            }
        }
    }
}