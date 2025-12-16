package com.example.forcetrack.network.dto

import com.google.gson.annotations.SerializedName

// ========== AUTH REQUESTS ==========

data class LoginRequest(
    @SerializedName("correo")
    val correo: String,

    @SerializedName("contrasena")
    val contrasena: String
)

data class CreateUsuarioRequest(
    @SerializedName("nombre_usuario")
    val nombreUsuario: String,

    @SerializedName("correo")
    val correo: String,

    @SerializedName("contrasena")
    val contrasena: String
)

// ========== BLOQUE REQUESTS ==========

data class CreateBloqueRequest(
    @SerializedName("usuario_id")
    val usuarioId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("categoria")
    val categoria: String = "General",

    @SerializedName("es_publico")
    val esPublico: Boolean = false
)

data class UpdateBloqueRequest(
    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("categoria")
    val categoria: String? = null,

    @SerializedName("es_publico")
    val esPublico: Boolean? = null
)

// ========== DIA REQUESTS ==========

data class CreateDiaRequest(
    @SerializedName("bloque_id")
    val bloqueId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("notas")
    val notas: String? = null
)

data class UpdateDiaRequest(
    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("notas")
    val notas: String? = null,

    @SerializedName("bloque_id")
    val bloqueId: Int? = null
)

// ========== EJERCICIO REQUESTS ==========

data class CreateEjercicioRequest(
    @SerializedName("dia_id")
    val diaId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descanso_segundos")
    val descansoSegundos: Int = 90
)

data class UpdateEjercicioRequest(
    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("descanso_segundos")
    val descansoSegundos: Int? = null,

    @SerializedName("dia_id")
    val diaId: Int? = null
)

// ========== SERIE REQUESTS ==========

data class CreateSerieRequest(
    @SerializedName("ejercicio_id")
    val ejercicioId: Int,

    @SerializedName("peso")
    val peso: Double = 0.0,

    @SerializedName("repeticiones")
    val repeticiones: Int = 0,

    @SerializedName("rir")
    val rir: Int = 0,

    @SerializedName("completada")
    val completada: Boolean = false
)

data class UpdateSerieRequest(
    @SerializedName("peso")
    val peso: Double? = null,

    @SerializedName("repeticiones")
    val repeticiones: Int? = null,

    @SerializedName("rir")
    val rir: Int? = null,

    @SerializedName("completada")
    val completada: Boolean? = null
)

// ========== EJERCICIO DISPONIBLE DTO ==========

data class EjercicioDisponibleDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("categoria")
    val categoria: String,

    @SerializedName("grupo_muscular")
    val grupoMuscular: String,

    @SerializedName("descripcion")
    val descripcion: String? = null
)

// ========== TRAINING LOG ==========

data class TrainingLogDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("usuario_id")
    val usuarioId: Int,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("bloque_id")
    val bloqueId: Int? = null,

    @SerializedName("dia_id")
    val diaId: Int? = null,

    @SerializedName("duracion_minutos")
    val duracionMinutos: Int? = null,

    @SerializedName("notas")
    val notas: String? = null
)

data class CreateTrainingLogRequest(
    @SerializedName("usuario_id")
    val usuarioId: Int,

    @SerializedName("fecha")
    val fecha: String,

    @SerializedName("bloque_id")
    val bloqueId: Int? = null,

    @SerializedName("dia_id")
    val diaId: Int? = null,

    @SerializedName("duracion_minutos")
    val duracionMinutos: Int? = null,

    @SerializedName("notas")
    val notas: String? = null
)

