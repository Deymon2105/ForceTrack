package com.example.forcetrack.network.dto

import com.google.gson.annotations.SerializedName

// ========== BLOQUE DTO ==========

data class BloqueDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String = "General",
    @SerializedName("es_publico") val esPublico: Boolean = false,
    @SerializedName("dias") val dias: List<DiaDto>? = null
)

// ========== DIA DTO ==========

data class DiaDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("bloque_id") val bloqueId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("numero_semana") val numeroSemana: Int = 1,
    @SerializedName("completado") val completado: Boolean? = false,
    @SerializedName("fecha_completado") val fechaCompletado: String? = null,
    @SerializedName("ejercicios") val ejercicios: List<EjercicioDto>? = null
)

// ========== EJERCICIO DTO ==========

data class EjercicioDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("dia_id") val diaId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descanso_segundos") val descansoSegundos: Int = 90,
    @SerializedName("series") val series: List<SerieDto>? = null
)

// ========== SERIE DTO ==========

data class SerieDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("ejercicio_id") val ejercicioId: Int,
    @SerializedName("peso") val peso: Double = 0.0,
    @SerializedName("repeticiones") val repeticiones: Int = 0,
    @SerializedName("rir") val rir: Int = 0,
    @SerializedName("completada") val completada: Boolean = false
)

// ========== RESPUESTA GENÉRICA ==========

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null
)

// ========== BLOQUE PÚBLICO DTO ==========

data class BloquePublicoDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("usuario_id") val usuarioId: Int = 0,
    @SerializedName("nombre_usuario") val nombreUsuario: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("categoria") val categoria: String = "General",
    @SerializedName("es_publico") val esPublico: Boolean = false,
    @SerializedName("cantidad_dias") val cantidadDias: Int = 0
)

// ========== SYNC DTOs ==========

data class SyncRequestDto(
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("last_sync") val lastSync: Long? = null,
    @SerializedName("bloques") val bloques: List<BloqueDto>? = null
)

data class SyncResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("bloques") val bloques: List<BloqueDto>? = null,
    @SerializedName("timestamp") val timestamp: Long
)

// ========== ESTADÍSTICAS DTOs ==========

data class EstadisticasDiaDto(
    @SerializedName("dia_id") val diaId: Int,
    @SerializedName("nombre_dia") val nombreDia: String,
    @SerializedName("fecha_completado") val fechaCompletado: String? = null,
    @SerializedName("total_ejercicios") val totalEjercicios: Int,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("total_series_completadas") val totalSeriesCompletadas: Int,
    @SerializedName("volumen_total") val volumenTotal: Double,
    @SerializedName("peso_promedio") val pesoPromedio: Double,
    @SerializedName("repeticiones_totales") val repeticionesTotales: Int,
    @SerializedName("intensidad_promedio") val intensidadPromedio: Double,
    @SerializedName("estadisticas_por_ejercicio") val estadisticasPorEjercicio: List<EstadisticaEjercicioDto>? = null
)

data class EstadisticaEjercicioDto(
    @SerializedName("ejercicio_id") val ejercicioId: Int,
    @SerializedName("nombre_ejercicio") val nombreEjercicio: String,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("series_completadas") val seriesCompletadas: Int,
    @SerializedName("volumen") val volumen: Double,
    @SerializedName("peso_maximo") val pesoMaximo: Double,
    @SerializedName("peso_promedio") val pesoPromedio: Double,
    @SerializedName("repeticiones_totales") val repeticionesTotales: Int,
    @SerializedName("intensidad_promedio") val intensidadPromedio: Double
)

data class EstadoDiaDto(
    @SerializedName("dia_id") val diaId: Int,
    @SerializedName("nombre_dia") val nombreDia: String,
    @SerializedName("completado") val completado: Boolean? = false,
    @SerializedName("fecha_completado") val fechaCompletado: String? = null,
    @SerializedName("puede_completarse") val puedeCompletarse: Boolean,
    @SerializedName("total_ejercicios") val totalEjercicios: Int,
    @SerializedName("total_series") val totalSeries: Int,
    @SerializedName("mensaje") val mensaje: String
)

data class TerminarDiaResponseDto(
    @SerializedName("exito") val exito: Boolean,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("dia_id") val diaId: Int? = null,
    @SerializedName("nombre_dia") val nombreDia: String? = null,
    @SerializedName("fecha_completado") val fechaCompletado: String? = null,
    @SerializedName("estadisticas") val estadisticas: EstadisticasDiaDto? = null
)
