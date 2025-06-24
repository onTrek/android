package com.ontrek.mobile.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.collectAsState
import com.ontrek.mobile.R
import com.ontrek.mobile.ui.theme.OnTrekTheme
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AuthScreen(navController: () -> Unit = {}) {
    val viewModel = viewModel<AuthViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        if (uiState.errorMessage != null || uiState.successMessage != null) {
            Toast.makeText(
                context,
                uiState.errorMessage ?: uiState.successMessage ?: "",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.clearMessages()
        }
    }

    Scaffold()
    {
        paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                    onValueChange = { viewModel.updateEmail(it) },
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
                            onValueChange = { viewModel.updateUsername(it) },
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
                    onValueChange = { viewModel.updatePassword(it) },
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
                            onClick = { viewModel.togglePasswordVisibility() },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = if (uiState.passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    visualTransformation = if (uiState.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            onValueChange = { viewModel.updatePasswordRepeat(it) },
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
                                    onClick = { viewModel.togglePasswordRepeatVisibility() },
                                    enabled = !uiState.isLoading
                                ) {
                                    Icon(
                                        imageVector = if (uiState.passwordRepeatVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle repeat password visibility",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            },
                            visualTransformation = if (uiState.passwordRepeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            viewModel.loginFunc()
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
            }

            // Testo e azione per cambiare modalità
            TextButton(
                onClick = { viewModel.switchAuthMode() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 10.dp),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.authMode == AuthMode.LOGIN) "Sign up" else "Log in")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    OnTrekTheme {
        AuthScreen()
    }
}