package com.example.forcetrack.network.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO para Bloque de Entrenamiento
 */
data class BloqueDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("semanas")
    val semanas: List<SemanaDto>? = null
)

/**
 * DTO para Semana de Entrenamiento
 */
data class SemanaDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("bloqueId")
    val bloqueId: Int,

    @SerializedName("numeroSemana")
    val numeroSemana: Int,

    @SerializedName("dias")
    val dias: List<DiaDto>? = null
)

/**
 * DTO para Día de Entrenamiento
 */
data class DiaDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("semanaId")
    val semanaId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("notas")
    val notas: String? = null,

    @SerializedName("ejercicios")
    val ejercicios: List<EjercicioDto>? = null
)

/**
 * DTO para Ejercicio
 */
data class EjercicioDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("diaId")
    val diaId: Int,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("descansoSegundos")
    val descansoSegundos: Int = 90,

    @SerializedName("series")
    val series: List<SerieDto>? = null
)

/**
 * DTO para Serie
 */
data class SerieDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("ejercicioId")
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

/**
 * DTO para respuesta genérica
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null
)

/**
 * DTO para sincronización de datos
 */
data class SyncRequestDto(
    @SerializedName("usuarioId")
    val usuarioId: Int,

    @SerializedName("lastSync")
    val lastSync: Long? = null, // Timestamp

    @SerializedName("bloques")
    val bloques: List<BloqueDto>? = null
)

data class SyncResponseDto(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("bloques")
    val bloques: List<BloqueDto>? = null,

    @SerializedName("timestamp")
    val timestamp: Long
)

