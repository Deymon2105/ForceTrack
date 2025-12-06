package com.example.forcetrack.network.api

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * Interfaz de API REST para el Backend (Spring Boot)
 * Compatible con los endpoints CRUD estándar
 */
interface XanoApi {

    // ========== AUTENTICACIÓN ==========

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<UsuarioDto>

    @POST("auth/signup")
    suspend fun signup(@Body request: CreateUsuarioRequest): Response<UsuarioDto>

    // ========== USUARIOS ==========

    @GET("usuario")
    suspend fun getAllUsuarios(): Response<List<UsuarioDto>>

    @POST("usuario")
    suspend fun createUsuario(@Body request: CreateUsuarioRequest): Response<UsuarioDto>

    @GET("usuario/{usuario_id}")
    suspend fun getUsuario(@Path("usuario_id") usuarioId: Int): Response<UsuarioDto>

    @PATCH("usuario/{usuario_id}")
    suspend fun updateUsuario(
        @Path("usuario_id") usuarioId: Int,
        @Body request: UpdateUsuarioRequest
    ): Response<UsuarioDto>

    @DELETE("usuario/{usuario_id}")
    suspend fun deleteUsuario(@Path("usuario_id") usuarioId: Int): Response<Unit>

    // ========== BLOQUES ==========

    @GET("bloque")
    suspend fun getAllBloques(): Response<List<BloqueDto>>

    @POST("bloque")
    suspend fun createBloque(@Body request: CreateBloqueRequest): Response<BloqueDto>

    @GET("bloque/{bloque_id}")
    suspend fun getBloque(@Path("bloque_id") bloqueId: Int): Response<BloqueDto>

    @PUT("bloque/{bloque_id}")
    suspend fun updateBloque(
        @Path("bloque_id") bloqueId: Int,
        @Body request: UpdateBloqueRequest
    ): Response<BloqueDto>

    @DELETE("bloque/{bloque_id}")
    suspend fun deleteBloque(@Path("bloque_id") bloqueId: Int): Response<Unit>
    
    // Obtener bloques públicos
    @GET("bloque/publicos")
    suspend fun getBloquesPublicos(
        @Query("categoria") categoria: String? = null
    ): Response<List<BloquePublicoDto>>
    
    // Cambiar visibilidad de un bloque
    @PATCH("bloque/{bloque_id}/visibilidad")
    suspend fun cambiarVisibilidadBloque(
        @Path("bloque_id") bloqueId: Int,
        @Query("esPublico") esPublico: Boolean
    ): Response<BloqueDto>

    // ========== DÍAS ==========

    @GET("dia")
    suspend fun getAllDias(): Response<List<DiaDto>>

    @POST("dia")
    suspend fun createDia(@Body request: CreateDiaRequest): Response<DiaDto>

    @GET("dia/{dia_id}")
    suspend fun getDia(@Path("dia_id") diaId: Int): Response<DiaDto>

    @PATCH("dia/{dia_id}")
    suspend fun updateDia(
        @Path("dia_id") diaId: Int,
        @Body request: UpdateDiaRequest
    ): Response<DiaDto>

    @DELETE("dia/{dia_id}")
    suspend fun deleteDia(@Path("dia_id") diaId: Int): Response<Unit>

    // ========== EJERCICIOS ==========

    @GET("ejercicio")
    suspend fun getAllEjercicios(): Response<List<EjercicioDto>>

    @POST("ejercicio")
    suspend fun createEjercicio(@Body request: CreateEjercicioRequest): Response<EjercicioDto>

    @GET("ejercicio/{ejercicio_id}")
    suspend fun getEjercicio(@Path("ejercicio_id") ejercicioId: Int): Response<EjercicioDto>

    @PATCH("ejercicio/{ejercicio_id}")
    suspend fun updateEjercicio(
        @Path("ejercicio_id") ejercicioId: Int,
        @Body request: UpdateEjercicioRequest
    ): Response<EjercicioDto>

    @DELETE("ejercicio/{ejercicio_id}")
    suspend fun deleteEjercicio(@Path("ejercicio_id") ejercicioId: Int): Response<Unit>

    // ========== SERIES ==========

    @GET("serie")
    suspend fun getAllSeries(): Response<List<SerieDto>>

    @POST("serie")
    suspend fun createSerie(@Body request: CreateSerieRequest): Response<SerieDto>

    @GET("serie/{serie_id}")
    suspend fun getSerie(@Path("serie_id") serieId: Int): Response<SerieDto>

    @PATCH("serie/{serie_id}")
    suspend fun updateSerie(
        @Path("serie_id") serieId: Int,
        @Body request: UpdateSerieRequest
    ): Response<SerieDto>

    @DELETE("serie/{serie_id}")
    suspend fun deleteSerie(@Path("serie_id") serieId: Int): Response<Unit>
}

// ========== REQUEST DTOs ==========

// Usuario Requests
data class CreateUsuarioRequest(
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String
)

data class UpdateUsuarioRequest(
    @SerializedName("nombre_usuario") val nombreUsuario: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("contrasena") val contrasena: String? = null
)

data class LoginRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String
)

// Bloque Requests
data class CreateBloqueRequest(
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String = "General",
    @SerializedName("es_publico") val esPublico: Boolean = false
)

data class UpdateBloqueRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("categoria") val categoria: String? = null,
    @SerializedName("es_publico") val esPublico: Boolean? = null
)

// Dia Requests
data class CreateDiaRequest(
    @SerializedName("bloque_id") val bloqueId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("notas") val notas: String? = null
)

data class UpdateDiaRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("bloque_id") val bloqueId: Int? = null
)

// Ejercicio Requests
data class CreateEjercicioRequest(
    @SerializedName("dia_id") val diaId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descanso_segundos") val descansoSegundos: Int = 90
)

data class UpdateEjercicioRequest(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("descanso_segundos") val descansoSegundos: Int? = null,
    @SerializedName("dia_id") val diaId: Int? = null
)

// Serie Requests
data class CreateSerieRequest(
    @SerializedName("ejercicio_id") val ejercicioId: Int,
    @SerializedName("peso") val peso: Double = 0.0,
    @SerializedName("repeticiones") val repeticiones: Int = 0,
    @SerializedName("rir") val rir: Int = 0,
    @SerializedName("completada") val completada: Boolean = false
)

data class UpdateSerieRequest(
    @SerializedName("peso") val peso: Double? = null,
    @SerializedName("repeticiones") val repeticiones: Int? = null,
    @SerializedName("rir") val rir: Int? = null,
    @SerializedName("completada") val completada: Boolean? = null,
    @SerializedName("ejercicio_id") val ejercicioId: Int? = null
)

// ========== RESPONSE DTOs ==========

data class UsuarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("created_at") val created_at: String? = null
)

data class BloqueDto(
    @SerializedName("id") val id: Int,
    @SerializedName("usuario_id") val usuarioId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String? = null,
    @SerializedName("es_publico") val esPublico: Boolean? = null,
    @SerializedName("created_at") val created_at: String? = null
)

data class BloquePublicoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("nombreUsuario") val nombreUsuario: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("esPublico") val esPublico: Boolean,
    @SerializedName("cantidadDias") val cantidadDias: Int
)

data class DiaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("bloque_id") val bloqueId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("notas") val notas: String? = null,
    @SerializedName("created_at") val created_at: String? = null
)

data class EjercicioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("dia_id") val diaId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descanso_segundos") val descansoSegundos: Int,
    @SerializedName("created_at") val created_at: String? = null
)

data class SerieDto(
    @SerializedName("id") val id: Int,
    @SerializedName("ejercicio_id") val ejercicioId: Int,
    @SerializedName("peso") val peso: Double,
    @SerializedName("repeticiones") val repeticiones: Int,
    @SerializedName("rir") val rir: Int,
    @SerializedName("completada") val completada: Boolean,
    @SerializedName("created_at") val created_at: String? = null
)
