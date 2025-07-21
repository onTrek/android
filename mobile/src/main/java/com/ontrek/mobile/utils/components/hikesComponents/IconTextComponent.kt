package com.ontrek.mobile.utils.components.hikesComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun IconTextComponent(
    text: String,
    icon: ImageVector = Icons.Default.TextFields,
    styleText: TextStyle = MaterialTheme.typography.bodyMedium,
    modifierIcon: Modifier = Modifier
        .padding(end = 8.dp)
        .width(24.dp),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Icon",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifierIcon
        )

        Text(
            text = text,
            style = styleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}