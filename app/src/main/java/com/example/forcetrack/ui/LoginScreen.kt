package com.example.forcetrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forcetrack.viewmodel.AuthViewModel
import com.example.forcetrack.viewmodel.AuthState

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel, 
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authUiState by authViewModel.uiState.collectAsState()

    // Limpia el error cuando el usuario empieza a escribir de nuevo
    LaunchedEffect(email, password) {
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
            Text("ForceTrack", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Nombre de Usuario o Correo") },
                isError = authUiState.authState == AuthState.ERROR,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                isError = authUiState.authState == AuthState.ERROR,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (authUiState.authState == AuthState.ERROR) {
                authUiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = authUiState.authState != AuthState.LOADING
            ) {
                if (authUiState.authState == AuthState.LOADING) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Iniciar Sesión")
                }
            }

            TextButton(onClick = onRegisterClick) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}