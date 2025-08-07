package com.ontrek.mobile.utils.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToPhotos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun EmptyComponent(
    fillMaxSize: Boolean = true,
    title: String = "No Data",
    description: String = "There are no items to display.",
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.AddToPhotos
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .then(if (fillMaxSize) Modifier.fillMaxSize() else Modifier.fillMaxWidth()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "No Icon",
            modifier = Modifier.then(
                if (fillMaxSize) Modifier.size(64.dp) else Modifier.size(48.dp)
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = if (fillMaxSize) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleLarge
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}