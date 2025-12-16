package com.example.forcetrack.network.api

import com.example.forcetrack.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de la API REST del backend Spring Boot
 */
interface ApiService {

    // ========== AUTENTICACIÓN ==========

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioDto>

    @POST("usuario")
    suspend fun createUsuario(@Body request: CreateUsuarioRequest): Response<UsuarioDto>

    // ========== BLOQUES ==========

    @GET("bloque/usuario/{usuarioId}")
    suspend fun getAllBloques(@Path("usuarioId") usuarioId: Int): Response<List<BloqueDto>>

    @POST("bloque")
    suspend fun createBloque(@Body request: CreateBloqueRequest): Response<BloqueDto>

    @PUT("bloque/{id}")
    suspend fun updateBloque(
        @Path("id") bloqueId: Int,
        @Body request: UpdateBloqueRequest
    ): Response<BloqueDto>

    @DELETE("bloque/{id}")
    suspend fun deleteBloque(@Path("id") bloqueId: Int): Response<Unit>

    @PATCH("bloque/{id}/visibilidad")
    suspend fun cambiarVisibilidadBloque(
        @Path("id") bloqueId: Int,
        @Query("esPublico") esPublico: Boolean
    ): Response<BloqueDto>

    @GET("bloque/publicos")
    suspend fun getBloquesPublicos(
        @Query("categoria") categoria: String? = null
    ): Response<List<BloquePublicoDto>>

    @POST("bloque/{id}/clonar")
    suspend fun clonarBloque(
        @Path("id") bloqueId: Int,
        @Query("usuarioId") usuarioId: Int
    ): Response<BloqueDto>

    // ========== DIAS ==========

    @GET("dia/bloque/{bloqueId}")
    suspend fun getDiasByBloque(@Path("bloqueId") bloqueId: Int): Response<List<DiaDto>>

    @POST("dia")
    suspend fun createDia(@Body request: CreateDiaRequest): Response<DiaDto>

    @PUT("dia/{id}")
    suspend fun updateDia(
        @Path("id") diaId: Int,
        @Body request: UpdateDiaRequest
    ): Response<DiaDto>

    @DELETE("dia/{id}")
    suspend fun deleteDia(@Path("id") diaId: Int): Response<Unit>

    @PUT("dia/{id}/terminar")
    suspend fun terminarDia(@Path("id") diaId: Int): Response<com.example.forcetrack.network.dto.TerminarDiaResponseDto>
    
    @POST("dia/{id}/completar")
    suspend fun completarDia(@Path("id") diaId: Int): Response<com.example.forcetrack.network.dto.EstadisticasDiaDto>

    @GET("dia/{id}/estado")
    suspend fun obtenerEstadoDia(@Path("id") diaId: Int): Response<com.example.forcetrack.network.dto.EstadoDiaDto>

    @GET("dia/{id}/estadisticas")
    suspend fun obtenerEstadisticasDia(@Path("id") diaId: Int): Response<com.example.forcetrack.network.dto.EstadisticasDiaDto>

    // ========== EJERCICIOS ==========

    @GET("ejercicio/dia/{diaId}")
    suspend fun getEjerciciosByDia(@Path("diaId") diaId: Int): Response<List<EjercicioDto>>

    @POST("ejercicio")
    suspend fun createEjercicio(@Body request: CreateEjercicioRequest): Response<EjercicioDto>

    @PUT("ejercicio/{id}")
    suspend fun updateEjercicio(
        @Path("id") ejercicioId: Int,
        @Body request: UpdateEjercicioRequest
    ): Response<EjercicioDto>

    @DELETE("ejercicio/{id}")
    suspend fun deleteEjercicio(@Path("id") ejercicioId: Int): Response<Unit>

    // ========== SERIES ==========

    @GET("serie/ejercicio/{ejercicioId}")
    suspend fun getSeriesByEjercicio(@Path("ejercicioId") ejercicioId: Int): Response<List<SerieDto>>

    @POST("serie")
    suspend fun createSerie(@Body request: CreateSerieRequest): Response<SerieDto>

    @PUT("serie/{id}")
    suspend fun updateSerie(
        @Path("id") serieId: Int,
        @Body request: UpdateSerieRequest
    ): Response<SerieDto>

    @DELETE("serie/{id}")
    suspend fun deleteSerie(@Path("id") serieId: Int): Response<Unit>

    // ========== EJERCICIOS DISPONIBLES ==========

    @GET("ejercicio-disponible")
    suspend fun getAllEjerciciosDisponibles(): Response<List<EjercicioDisponibleDto>>

    @GET("ejercicio-disponible/{id}")
    suspend fun getEjercicioDisponible(@Path("id") id: Int): Response<EjercicioDisponibleDto>

    @GET("ejercicio-disponible/categoria/{categoria}")
    suspend fun getEjerciciosDisponiblesByCategoria(
        @Path("categoria") categoria: String
    ): Response<List<EjercicioDisponibleDto>>

    @GET("ejercicio-disponible/grupo/{grupoMuscular}")
    suspend fun getEjerciciosDisponiblesByGrupoMuscular(
        @Path("grupoMuscular") grupoMuscular: String
    ): Response<List<EjercicioDisponibleDto>>

    // ========== TRAINING LOGS ==========

    @GET("training-log/usuario/{usuarioId}")
    suspend fun getTrainingLogsByUsuario(@Path("usuarioId") usuarioId: Int): Response<List<TrainingLogDto>>

    @POST("training-log")
    suspend fun createTrainingLog(@Body request: CreateTrainingLogRequest): Response<TrainingLogDto>
}
