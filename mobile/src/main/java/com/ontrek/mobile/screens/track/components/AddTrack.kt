package com.ontrek.mobile.screens.track.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ontrek.shared.api.track.uploadTrack
import com.ontrek.shared.constant.MAX_TITLE_TRACK_LENGTH
import java.net.URLDecoder

@Composable
fun AddTrackDialog(
    onDismissRequest: () -> Unit,
    onTrackAdded: () -> Unit,
    fileUri: Uri
) {
    var title by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val getFileNameFromUri: (Context, Uri) -> String? = { context, uri ->
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex("_display_name")
            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }

    val fileName by remember {
        mutableStateOf(
            getFileNameFromUri(context, fileUri)?.let { URLDecoder.decode(it, "UTF-8") } ?: "file.gpx"
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add new Track",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= MAX_TITLE_TRACK_LENGTH) title = it
                    },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${title.length}/$MAX_TITLE_TRACK_LENGTH")  }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Selected file:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 0.dp, bottom = 2.dp)
                    )

                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }


                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Close")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            isUploading = true
                            errorMessage = null

                            fileUri.let { uri ->
                                try {
                                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                        val bytes = inputStream.readBytes()
                                        uploadTrack(
                                            fileName = fileName,
                                            titleTrack = title,
                                            gpxFileBytes = bytes,
                                            onSuccess = {
                                                isUploading = false
                                                onTrackAdded()
                                            },
                                            onError = { error ->
                                                isUploading = false
                                                errorMessage = error
                                            },
                                        )
                                    } ?: run {
                                        isUploading = false
                                        errorMessage = "Impossibile to read the file"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                }
                            }
                        },
                        enabled = title.isNotEmpty() && !isUploading
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Upload")
                        }
                    }
                }
            }
        }
    }
}