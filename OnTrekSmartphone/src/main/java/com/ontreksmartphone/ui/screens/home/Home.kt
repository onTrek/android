package com.ontreksmartphone.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ontreksmartphone.ui.theme.OnTrekSmartwatchTheme

@Composable
fun HomeScreen(
    name: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { innerPadding ->
            Text(
                text = "Welcome to the Home Screen! $name",
                modifier = Modifier.padding(innerPadding)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OnTrekSmartwatchTheme {
        HomeScreen("Developer", Modifier.fillMaxSize())
    }
}