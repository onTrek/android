package com.ontrek.mobile.utils.components.trackComponents

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun AddTrackDialog(
    onDismissRequest: () -> Unit,
    onTrackAdded: () -> Unit,
    token: String
) {
    var title by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(1024)  // Leggo i primi 1024 bytes
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val content = String(buffer, 0, bytesRead)
                        if (content.contains("<?xml") && content.contains("<gpx")) {
                            selectedFileUri = uri
                        } else {
                            errorMessage = "Select a valid GPX file"
                        }
                    } else {
                        errorMessage = "Empty file selected or not a valid GPX file"
                    }
                } ?: run {
                    errorMessage = "Impossible to read the file"
                }
            } catch (e: Exception) {
                errorMessage = "Error to read the file: ${e.message}"
                Log.e("AddTrack", "Errore nella lettura del file", e)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false)
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
                        if (it.length <= 64) title = it
                    },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${title.length}/64") }
                )

                Button(
                    onClick = { filePicker.launch("*/*")},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select GPX File")
                }

                selectedFileUri?.let {
                    Text(
                        text = "File selected: ${it.lastPathSegment ?: "file.gpx"}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

                            selectedFileUri?.let { uri ->
                                try {
                                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                        val bytes = inputStream.readBytes()
                                        uploadTrack(
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
                                            token = token
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
                        enabled = title.isNotEmpty() && selectedFileUri != null && !isUploading
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