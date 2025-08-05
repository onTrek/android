package com.ontrek.mobile.screens.profile.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.profile.ProfileViewModel

@Composable
fun ConnectionWearButton(
    connectionState: ProfileViewModel.ConnectionState,
    onConnectClick: () -> Unit,
) {
    Button(
        onClick = onConnectClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        enabled = when (connectionState) {
            is ProfileViewModel.ConnectionState.Success -> !connectionState.isConnected
            is ProfileViewModel.ConnectionState.Loading -> false
            is ProfileViewModel.ConnectionState.Error -> true
        }
    ) {
        when (connectionState) {
            is ProfileViewModel.ConnectionState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    strokeWidth = 2.dp
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Rounded.Watch,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (connectionState is ProfileViewModel.ConnectionState.Success && connectionState.isConnected)
                        "Smartwatch connected" else "Connect smartwatch",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}