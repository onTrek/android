package com.ontrek.mobile.screens.profile

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.profile.components.ConnectionWearButton
import com.ontrek.mobile.screens.profile.components.FriendsTab
import com.ontrek.mobile.screens.profile.components.ImageProfileDialog
import com.ontrek.mobile.screens.profile.components.MenuDialog
import com.ontrek.mobile.screens.profile.components.ProfileCard
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.ErrorComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    token: String,
    currentUser: String,
    clearToken: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()
    var showMenuDialog by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()
    val imageProfile by viewModel.imageProfile.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var previewImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var preview by remember { mutableStateOf(ByteArray(0)) }
    var modifyImageProfile by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFilename by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(imageProfile) {
        when (imageProfile) {
            is ProfileViewModel.UserImageState.Success -> {
                val imageData = (imageProfile as ProfileViewModel.UserImageState.Success).imageBytes
                if (imageData.isNotEmpty()) {
                    imageBitmap =
                        BitmapFactory.decodeByteArray(imageData, 0, imageData.size)?.asImageBitmap()
                }
            }

            else -> { /* Non fare nulla per gli altri stati */
            }
        }
    }

    LaunchedEffect(preview.contentHashCode()) {
        if (preview.isNotEmpty()) {
            previewImageBitmap =
                BitmapFactory.decodeByteArray(preview, 0, preview.size)?.asImageBitmap()
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

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        viewModel.fetchFriends()
        viewModel.loadFriendRequests()
    }

    if (msgToast.isNotEmpty()) {
        LaunchedEffect(msgToast) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.clearMsgToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Your profile") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { showMenuDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showMenuDialog) {
                MenuDialog(
                    onDismiss = { showMenuDialog = false },
                    onDeleteProfile = {
                        viewModel.deleteProfile(
                            clearToken = { clearToken() },
                        )
                        viewModel.setMsgToast("Your profile has been deleted")
                    },
                    onLogoutClick = {
                        clearToken()
                        viewModel.setMsgToast("You have been logged out")
                    }
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
                        if (selectedFilename != null) {
                            viewModel.updateProfileImage(preview, selectedFilename!!)
                        }
                    }
                )
            }

            when (userProfile) {
                is ProfileViewModel.UserProfileState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProfileViewModel.UserProfileState.Success -> {
                    val profile =
                        (userProfile as ProfileViewModel.UserProfileState.Success).userProfile

                    ProfileCard(
                        profile = profile,
                        imageBitmap = imageBitmap,
                        imageLoadingState = imageProfile,
                        onImageClick = { showFilePicker = true }
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    ConnectionWearButton(
                        connectionState = connectionStatus,
                        onConnectClick = {
                            viewModel.sendAuthToWearable(context, token, currentUser)
                        },
                    )

                    Spacer(modifier = Modifier.padding(vertical = 8.dp))

                    FriendsTab(
                        viewModel = viewModel,
                    )
                }

                is ProfileViewModel.UserProfileState.Error -> {
                    val errorMsg = (userProfile as ProfileViewModel.UserProfileState.Error).message
                    ErrorComponent(errorMsg = errorMsg)
                }
            }
        }
    }
}