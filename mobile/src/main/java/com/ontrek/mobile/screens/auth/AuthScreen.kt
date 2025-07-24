package com.ontrek.mobile.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ontrek.mobile.R
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.shared.data.AuthMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen() {
    val viewModel = viewModel<AuthViewModel>()
    val uiState by viewModel.authState.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()
    val context = LocalContext.current
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)
    var passwordVisibility by remember { mutableStateOf(false) }
    var passwordRepeatVisibility by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (msgToast.isNotEmpty()) {
        LaunchedEffect(msgToast) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.clearMsgToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo OnTrek",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.FillHeight
            )

            // Campo email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = {
                    viewModel.updateState(email = it)
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo username (solo per signup)
            AnimatedVisibility(
                visible = uiState.authMode == AuthMode.SIGNUP,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = {
                            viewModel.updateState(username = it)
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username Icon",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Campo password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = {
                    viewModel.updateState(password = it)
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                             passwordVisibility = !passwordVisibility
                        },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (uiState.authMode == AuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo ripeti password (solo per signup)
            AnimatedVisibility(
                visible = uiState.authMode == AuthMode.SIGNUP,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Column {
                    OutlinedTextField(
                        value = uiState.passwordRepeat,
                        onValueChange = {
                            viewModel.updateState(passwordRepeat = it)
                        },
                        label = { Text("Repeat password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Repeat Password Icon",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordRepeatVisibility = !passwordRepeatVisibility
                                },
                                enabled = !uiState.isLoading
                            ) {
                                Icon(
                                    imageVector = if (passwordRepeatVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle repeat password visibility",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        visualTransformation = if (passwordRepeatVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        enabled = !uiState.isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottone principale con indicatore di caricamento
            Button(
                onClick = {
                    if (uiState.authMode == AuthMode.LOGIN) {
                        viewModel.loginFunc(
                            saveToken = { token ->
                                preferencesViewModel.saveToken(token)
                            },
                            saveCurrentUser = { userId ->
                                preferencesViewModel.saveCurrentUser(userId)
                            })
                    } else {
                        viewModel.signUpFunc()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (uiState.authMode == AuthMode.LOGIN) "Login" else "Sign up")
                }
            }

            // Testo e azione per cambiare modalità
            TextButton(
                onClick = {
                    viewModel.updateState(
                        authMode = if (uiState.authMode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN
                    )
                },
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.authMode == AuthMode.LOGIN) "Sign up" else "Log in")
            }
        }
    }
}