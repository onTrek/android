package com.ontrek.mobile.screens.profile

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.BottomNavBar
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Profile(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingConnection by viewModel.isLoadingConnection.collectAsState()
    val isLoadingImage by viewModel.isLoadingImage.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState(initial = false)
    val msgToast by viewModel.msgToastFlow.collectAsState()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var preview by remember { mutableStateOf(ByteArray(0)) }
    var modifyImageProfile by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFilename by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)
    val token = preferencesViewModel.tokenState.collectAsStateWithLifecycle().value

    val isDevelopmentMode = false


    LaunchedEffect(userProfile.imageProfile.contentHashCode()) {
        if (userProfile.imageProfile.isNotEmpty()) {
            imageBitmap = BitmapFactory.decodeByteArray(
                userProfile.imageProfile,
                0,
                userProfile.imageProfile.size
            )?.asImageBitmap()
        }
    }

    LaunchedEffect(preview.contentHashCode()) {
        if (preview.isNotEmpty()) {
            previewImageBitmap = BitmapFactory.decodeByteArray(
                preview,
                0,
                preview.size
            )?.asImageBitmap()
        }
    }

    LaunchedEffect(imageBitmap) {
        modifyImageProfile = false
        preview = ByteArray(0)
        previewImageBitmap = null
        selectedFilename = null
    }

    LaunchedEffect(selectedFilename) {
        if (selectedFilename != null) {
            modifyImageProfile = true
        }
    }


    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val fileName = viewModel.getFileNameFromUri(context, uri)

                val extension =
                    fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()

                if (extension == "jpg" || extension == "jpeg" || extension == "png") {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val imageBytes = inputStream.readBytes()

                        if (imageBytes.size > 4) {
                            val isPng =
                                imageBytes[0] == 0x89.toByte() && imageBytes[1] == 0x50.toByte() &&
                                        imageBytes[2] == 0x4E.toByte() && imageBytes[3] == 0x47.toByte()
                            val isJpeg =
                                imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte()

                            if (isPng || isJpeg) {
                                selectedFilename = fileName
                                preview = imageBytes
                            } else {
                                errorMessage = "File is not a valid image"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            errorMessage = "Empty or too small file"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        errorMessage = "Impossible to read the file"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    errorMessage = "Invalid file extension: must be PNG or JPEG"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                errorMessage = "Error reading the file: ${e.message}"
                Log.e("ImageProfileUpload", "Error reading file", e)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(showFilePicker) {
        if (showFilePicker) {
            filePicker.launch("*/*")
            showFilePicker = false

        }
    }

    LaunchedEffect(token) {
        if (!token.isNullOrEmpty()) {
            viewModel.fetchUserProfile()
        } else {
            Toast.makeText(context, "Token not vaild", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(msgToast) {
        if (msgToast.isNotEmpty()) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.resetMsgToast()
        }
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "User profile")
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout(
                                clearToken = {
                                    preferencesViewModel.clearToken()
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                                    .clickable { showFilePicker = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoadingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp
                                    )
                                } else if (userProfile.imageProfile.isNotEmpty() && imageBitmap != null) {
                                    Image(
                                        bitmap = imageBitmap!!,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit profile image",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Username",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${userProfile.username}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = userProfile.email,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // ID utente (solo in modalità sviluppo)
                        if (isDevelopmentMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "ID: ${userProfile.userId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = { viewModel.sendAuthToWearable(context, token!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        enabled = !connectionStatus && !isLoadingConnection
                    ) {
                        if (isLoadingConnection) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Watch,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (connectionStatus) "Smartwatch connected" else "Connect smartwatch",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    TextButton(
                        onClick = { showDeleteDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete profile",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (modifyImageProfile) {
                    Dialog(
                        onDismissRequest = { modifyImageProfile = false },
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
                                    text = "Modify image",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (previewImageBitmap != null) {
                                        Image(
                                            bitmap = previewImageBitmap!!,
                                            contentDescription = "Profile image",
                                            modifier = Modifier
                                                .size(160.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop

                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(160.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth().padding(8.dp)

                            ) {
                                TextButton(onClick = {
                                    modifyImageProfile = false
                                    preview = ByteArray(0)
                                    previewImageBitmap = imageBitmap
                                    selectedFilename = null
                                }) {
                                    Text(
                                        text = "Cancel",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        viewModel.updateProfileImage(
                                            preview,
                                            selectedFilename!!
                                        )
                                    },
                                ) {
                                    Text(
                                        text = "Confirm",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    title = "Delete Profile",
                    text = "Are you sure you want to delete your profile? This action cannot be undone.",
                    onConfirm = {
                        viewModel.fetchDeleteProfile(
                            clearToken = {
                                preferencesViewModel.clearToken()
                            },
                        )
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false },
                )
            }
        }


    }
}
