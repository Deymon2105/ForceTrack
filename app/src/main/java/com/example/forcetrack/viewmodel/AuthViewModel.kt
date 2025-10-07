package com.example.forcetrack.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.forcetrack.model.Usuario
import com.example.forcetrack.repository.MockRepository

class AuthViewModel : ViewModel() {
    val loggedIn = mutableStateOf(false)

    fun login(email: String, password: String): Boolean {
        loggedIn.value = MockRepository.autenticar(email, password)
        return loggedIn.value
    }

    fun register(username: String, email: String, password: String) {
        MockRepository.registrarUsuario(Usuario(username, email, password))
    }
}
