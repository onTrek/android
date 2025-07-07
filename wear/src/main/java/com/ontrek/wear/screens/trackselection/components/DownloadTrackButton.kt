package com.ontrek.wear.screens.trackselection.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.Text
import com.ontrek.wear.utils.components.Loading
import kotlinx.coroutines.flow.StateFlow

@Composable
fun DownloadTrackButton(
    modifier: Modifier = Modifier,
    trackName: String,
    trackID: Int,
    token: String,
    isDownloadingState: StateFlow<Boolean>,
    errorMessageState: StateFlow<String?>,
    onDownloadClick: (String, Int, Context) -> Unit = { _, _, _ -> },
) {

    val context = LocalContext.current

    val isDownloading by isDownloadingState.collectAsStateWithLifecycle()
    val errorMessage by errorMessageState.collectAsStateWithLifecycle()

    OutlinedButton(
        onClick = {
            onDownloadClick(token, trackID, context)
//            composableScope.launch {
//                navController.navigate(route = Screen.TrackScreen.route + "?trackID=${trackID}")
//            }

        },
        modifier = modifier
    ) {
        if (isDownloading) {
            Loading(Modifier.fillMaxWidth())
        } else if (errorMessage != null) {
            LaunchedEffect(errorMessage) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        } else {
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
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download track",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.15f)
            )
        }
    }
}