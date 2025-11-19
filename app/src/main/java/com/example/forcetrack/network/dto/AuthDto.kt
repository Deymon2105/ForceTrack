package com.example.forcetrack.network.dto

/**
 * ⚠️ ARCHIVO OBSOLETO - NO USAR
 *
 * Este archivo ha sido reemplazado por los DTOs en:
 * com.example.forcetrack.network.api.XanoApi
 *
 * Los DTOs ahora están todos en XanoApi.kt para mejor organización.
 * RemoteRepository hace la conversión automáticamente.
 *
 * Este archivo se mantiene temporalmente solo por compatibilidad.
 * Será eliminado en futuras versiones.
 */

// Los DTOs de Auth ahora están en XanoApi.kt
// Usa: com.example.forcetrack.network.api.UsuarioDto
// Usa: com.example.forcetrack.network.api.LoginRequestDto
// Usa: com.example.forcetrack.network.api.RegistroRequestDto

/**
 * DTOs para autenticación
 */

data class UsuarioDto(
    val id: Int,
    val nombreUsuario: String,
    val correo: String,
    val contrasena: String? = null
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String,
    val usuario: UsuarioDto? = null,
    val token: String? = null
)

data class LoginRequestDto(
    val nombreUsuario: String,
    val contrasena: String
)

data class RegisterRequestDto(
    val nombreUsuario: String,
    val correo: String,
    val contrasena: String
)
