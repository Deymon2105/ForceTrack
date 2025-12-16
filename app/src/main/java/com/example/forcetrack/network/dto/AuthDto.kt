package com.example.forcetrack.network.dto

import com.google.gson.annotations.SerializedName

/**
 * ⚠️ ARCHIVO OBSOLETO - NO USAR
 *
 * Este archivo ha sido reemplazado por los DTOs en:
 * com.example.forcetrack.network.api.ApiService
 *
 * Los DTOs ahora están organizados por entidad.
 *
 * Este archivo se mantiene temporalmente solo por compatibilidad.
 * Será eliminado en futuras versiones.
 */

// Los DTOs de Auth ahora están en archivos separados
// Usa: com.example.forcetrack.network.dto.UsuarioDto
// Usa: com.example.forcetrack.network.dto.LoginRequest
// Usa: com.example.forcetrack.network.dto.CreateUsuarioRequest

/**
 * DTOs para autenticación
 */

data class UsuarioDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nombre_usuario")
    val nombreUsuario: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("contrasena")
    val contrasena: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
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
