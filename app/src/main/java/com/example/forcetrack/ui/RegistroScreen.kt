package com.example.forcetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.forcetrack.viewmodel.AuthViewModel

@Composable
fun RegistroScreen(authViewModel: AuthViewModel, onRegisterSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(40.dp))
        Text("ForceTrack", style = MaterialTheme.typography.headlineLarge)
        Text("Registro", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Nombre de Usuario") })
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            authViewModel.register(username, correo, password)
            onRegisterSuccess()
        }) {
            Text("Registrarse")
        }
    }
}
