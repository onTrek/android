package com.ontrek.wear.screens.trackselection.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ontrek.wear.screens.Screen

@Composable
fun TrackButton(
    modifier: Modifier = Modifier,
    trackName: String,
    trackID: Int,
    navController: NavHostController,
) {
    Button(
        onClick = {
            navController.navigate(route = Screen.TrackScreen.route + "?trackID=${trackID}")
        },
        onLongClick = {
            // TODO: Alert dialog to confirm deletion
        },
        modifier = modifier
    ) {
        Text(
            text = trackName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .weight(0.85f)
                .padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
