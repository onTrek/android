package com.ontrek.mobile.screens.profile

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.profile.components.ConnectionWearButton
import com.ontrek.mobile.screens.profile.components.ImageProfileDialog
import com.ontrek.mobile.screens.profile.components.ProfileCard
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.ErrorViewComponent
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, tokenState: StateFlow<String?>) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()
    val imageProfile by viewModel.imageProfile.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val token by tokenState.collectAsStateWithLifecycle()
    val msgToast by viewModel.msgToast.collectAsState()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var preview by remember { mutableStateOf(ByteArray(0)) }
    var modifyImageProfile by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFilename by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val preferencesViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory)

    LaunchedEffect(imageProfile) {
        when (imageProfile) {
            is ProfileViewModel.UserImageState.Success -> {
                val imageData = (imageProfile as ProfileViewModel.UserImageState.Success).imageBytes
                if (imageData.isNotEmpty()) {
                    imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)?.asImageBitmap()
                }
            }
            else -> { /* Non fare nulla per gli altri stati */ }
        }
    }

    LaunchedEffect(preview.contentHashCode()) {
        if (preview.isNotEmpty()) {
            previewImageBitmap = BitmapFactory.decodeByteArray(preview, 0, preview.size)?.asImageBitmap()
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
                val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase()

                if (extension == "jpg" || extension == "jpeg" || extension == "png") {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val imageBytes = inputStream.readBytes()

                        if (imageBytes.size > 4) {
                            val isPng = imageBytes[0] == 0x89.toByte() && imageBytes[1] == 0x50.toByte() &&
                                    imageBytes[2] == 0x4E.toByte() && imageBytes[3] == 0x47.toByte()
                            val isJpeg = imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte()

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
            viewModel.fetchUserProfile(token!!)
        } else {
            Toast.makeText(context, "Token not valid", Toast.LENGTH_SHORT).show()
        }
    }

    if (msgToast.isNotEmpty()) {
        LaunchedEffect(msgToast) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.clearMsgToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "User profile") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
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
            if (showLogoutDialog) {
                DeleteConfirmationDialog(
                    title = "Logout",
                    text = "Are you sure you want to logout?",
                    textButton = "Logout",
                    onConfirm = {
                        viewModel.logout { preferencesViewModel.clearToken() }
                        showLogoutDialog = false
                    },
                    onDismiss = { showLogoutDialog = false },
                )
            }

            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    title = "Delete Profile",
                    text = "Are you sure you want to delete your profile? This action cannot be undone.",
                    onConfirm = {
                        if (token != null) {
                            viewModel.fetchDeleteProfile(
                                clearToken = { preferencesViewModel.clearToken() },
                                token!!
                            )
                        }
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false },
                )
            }

            if (modifyImageProfile) {
                ImageProfileDialog(
                    previewImageBitmap = previewImageBitmap,
                    onDismiss = {
                        modifyImageProfile = false
                        preview = ByteArray(0)
                        previewImageBitmap = null
                        selectedFilename = null
                    },
                    onConfirm = {
                        if (token != null && selectedFilename != null) {
                            viewModel.updateProfileImage(token!!, preview, selectedFilename!!)
                        }
                    }
                )
            }

            when (userProfile) {
                is ProfileViewModel.UserProfileState.Loading -> {
                    CircularProgressIndicator()
                }
                is ProfileViewModel.UserProfileState.Success -> {
                    val profile = (userProfile as ProfileViewModel.UserProfileState.Success).userProfile

                    ProfileCard(
                        profile = profile,
                        imageBitmap = imageBitmap,
                        imageLoadingState = imageProfile,
                        onImageClick = { showFilePicker = true }
                    )

                    ConnectionWearButton(
                        connectionState = connectionStatus,
                        onConnectClick = {
                            if (token != null) viewModel.sendAuthToWearable(context, token!!)
                        },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }
                is ProfileViewModel.UserProfileState.Error -> {
                    val errorMsg = (userProfile as ProfileViewModel.UserProfileState.Error).message
                    ErrorViewComponent(errorMsg = errorMsg)
                }
            }
        }
    }
}