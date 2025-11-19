package com.example.forcetrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.forcetrack.utils.ValidationUtils
import com.example.forcetrack.viewmodel.AuthViewModel
import com.example.forcetrack.viewmodel.AuthState

@Composable
fun RegistroScreen(
    authViewModel: AuthViewModel,
    onBackToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showValidation by remember { mutableStateOf(false) }

    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(username, email, password) {
        if (authUiState.authState == AuthState.ERROR) {
            authViewModel.clearError()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Crear Cuenta",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            // Campo de nombre de usuario
            OutlinedTextField(
                value = username,
                onValueChange = {
                    // Solo permitir letras, números y guiones bajos
                    if (it.all { char -> char.isLetterOrDigit() || char == '_' }) {
                        username = it
                    }
                },
                label = { Text("Nombre de Usuario") },
                isError = showValidation && ValidationUtils.getUsernameErrorMessage(username) != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (showValidation) {
                        ValidationUtils.getUsernameErrorMessage(username)?.let {
                            Text(it)
                        }
                    } else {
                        Text("3-20 caracteres (letras, números, guiones bajos)")
                    }
                }
            )

            // Campo de correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Correo Electrónico") },
                isError = showValidation && ValidationUtils.getEmailErrorMessage(email) != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                supportingText = {
                    if (showValidation) {
                        ValidationUtils.getEmailErrorMessage(email)?.let {
                            Text(it)
                        }
                    } else {
                        Text("ejemplo@correo.com")
                    }
                }
            )

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                isError = showValidation && ValidationUtils.getPasswordErrorMessage(password) != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                supportingText = {
                    if (showValidation) {
                        ValidationUtils.getPasswordErrorMessage(password)?.let {
                            Text(it)
                        }
                    } else {
                        Text("Mínimo 6 caracteres")
                    }
                }
            )

            // Mensaje de error de autenticación
            if (authUiState.authState == AuthState.ERROR) {
                authUiState.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Botón de registro
            Button(
                onClick = {
                    showValidation = true
                    val validation = ValidationUtils.validateRegistration(username, email, password)
                    if (validation.isValid) {
                        authViewModel.register(username, email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authUiState.authState != AuthState.LOADING
            ) {
                if (authUiState.authState == AuthState.LOADING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrarse")
                }
            }

            TextButton(onClick = onBackToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}