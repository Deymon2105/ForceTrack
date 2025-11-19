package com.example.forcetrack.network.repository

import com.example.forcetrack.network.RetrofitClient
import com.example.forcetrack.network.RequestQueue
import com.example.forcetrack.network.api.*
import android.util.Log

/**
 * Repositorio para operaciones con Xano
 * Usa los endpoints REST estándar de Xano (GET, POST, PATCH, DELETE)
 * ⚡ AHORA CON SISTEMA ANTI-429: Todas las peticiones pasan por RequestQueue
 */
class XanoRepository {

    private val usuarioApi = RetrofitClient.usuarioApi
    private val bloqueApi = RetrofitClient.bloqueApi
    private val semanaApi = RetrofitClient.semanaApi
    private val diaApi = RetrofitClient.diaApi
    private val ejercicioApi = RetrofitClient.ejercicioApi
    private val serieApi = RetrofitClient.serieApi

    // ========== USUARIOS (AUTENTICACIÓN) ==========

    /**
     * Registrar nuevo usuario
     */
    suspend fun register(
        nombreUsuario: String,
        correo: String,
        contrasena: String
    ): Result<UsuarioDto> {
        return RequestQueue.execute(operation = {
            try {
                Log.d("XanoRepository", "🔧 Creando CreateUsuarioRequest:")
                Log.d("XanoRepository", "   nombreUsuario: $nombreUsuario")
                Log.d("XanoRepository", "   correo: $correo")
                Log.d("XanoRepository", "   contrasena: $contrasena")

                val request = CreateUsuarioRequest(nombreUsuario, correo, contrasena)
                Log.d("XanoRepository", "📤 Enviando request a Xano: $request")

                val response = usuarioApi.createUsuario(request)

                Log.d("XanoRepository", "📥 Respuesta recibida:")
                Log.d("XanoRepository", "   Code: ${response.code()}")
                Log.d("XanoRepository", "   Success: ${response.isSuccessful}")
                Log.d("XanoRepository", "   Body: ${response.body()}")
                Log.d("XanoRepository", "   ErrorBody: ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body() != null) {
                    Log.d("XanoRepository", "✅ Usuario creado exitosamente")
                    Result.success(response.body()!!)
                } else {
                    val errorMsg = "Registro falló: ${response.code()} - ${response.message()}"
                    Log.e("XanoRepository", "❌ $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("XanoRepository", "❌ Excepción en register: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        })
    }

    /**
     * Login - buscar usuario por correo y verificar contraseña
     */
    suspend fun login(correo: String, contrasena: String): Result<UsuarioDto> {
        return RequestQueue.execute(
            operation = {
                try {
                    Log.d("XanoRepository", "🔐 Intentando login con correo: $correo")

                    // Llamar al endpoint de autenticación de Xano
                    val response = usuarioApi.login(LoginRequest(correo, contrasena))

                    if (response.isSuccessful && response.body() != null) {
                        Log.d("XanoRepository", "✅ Usuario encontrado: ${response.body()!!.nombreUsuario}")
                        Result.success(response.body()!!)
                    } else {
                        Log.e("XanoRepository", "❌ Login falló: ${response.code()}")
                        Result.failure(Exception("Credenciales inválidas"))
                    }
                } catch (e: Exception) {
                    Log.e("XanoRepository", "❌ Error en login: ${e.message}")
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.HIGH
        )
    }

    // ========== BLOQUES ==========

    /**
     * Obtener todos los bloques (filtraremos por usuario en el cliente)
     */
    suspend fun obtenerBloques(usuarioId: Int): Result<List<BloqueDto>> {
        return RequestQueue.execute(
            operation = {
                try {
                    val response = bloqueApi.getAllBloques()

                    if (response.isSuccessful && response.body() != null) {
                        // Filtrar por usuario
                        val bloques = response.body()!!.filter { it.usuarioId == usuarioId }
                        Result.success(bloques)
                    } else {
                        Result.failure(Exception("Error obteniendo bloques: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.LOW
        )
    }

    /**
     * Crear un nuevo bloque
     */
    suspend fun crearBloque(usuarioId: Int, nombre: String): Result<BloqueDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = bloqueApi.createBloque(
                    CreateBloqueRequest(usuarioId, nombre)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error creando bloque: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    /**
     * Eliminar un bloque
     */
    suspend fun eliminarBloque(id: Int): Result<Unit> {
        return RequestQueue.execute(operation = {
            try {
                val response = bloqueApi.deleteBloque(id)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error eliminando bloque: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    // ========== SEMANAS ==========

    /**
     * Obtener semanas de un bloque
     */
    suspend fun obtenerSemanas(bloqueId: Int): Result<List<SemanaDto>> {
        return RequestQueue.execute(
            operation = {
                try {
                    val response = semanaApi.getAllSemanas()

                    if (response.isSuccessful && response.body() != null) {
                        val semanas = response.body()!!.filter { it.bloqueId == bloqueId }
                        Result.success(semanas)
                    } else {
                        Result.failure(Exception("Error obteniendo semanas: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.LOW
        )
    }

    /**
     * Crear una nueva semana
     */
    suspend fun crearSemana(bloqueId: Int, numeroSemana: Int): Result<SemanaDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = semanaApi.createSemana(
                    CreateSemanaRequest(bloqueId, numeroSemana)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error creando semana: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    // ========== DÍAS ==========

    /**
     * Obtener días de una semana
     */
    suspend fun obtenerDias(semanaId: Int): Result<List<DiaDto>> {
        return RequestQueue.execute(
            operation = {
                try {
                    val response = diaApi.getAllDias()

                    if (response.isSuccessful && response.body() != null) {
                        val dias = response.body()!!.filter { it.semanaId == semanaId }
                        Result.success(dias)
                    } else {
                        Result.failure(Exception("Error obteniendo días: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.LOW
        )
    }

    /**
     * Crear un nuevo día
     */
    suspend fun crearDia(semanaId: Int, nombre: String, notas: String? = null): Result<DiaDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = diaApi.createDia(
                    CreateDiaRequest(semanaId, nombre, notas)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error creando día: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    // ========== EJERCICIOS ==========

    /**
     * Obtener ejercicios de un día
     */
    suspend fun obtenerEjercicios(diaId: Int): Result<List<EjercicioDto>> {
        return RequestQueue.execute(
            operation = {
                try {
                    val response = ejercicioApi.getAllEjercicios()

                    if (response.isSuccessful && response.body() != null) {
                        val ejercicios = response.body()!!.filter { it.diaId == diaId }
                        Result.success(ejercicios)
                    } else {
                        Result.failure(Exception("Error obteniendo ejercicios: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.LOW
        )
    }

    /**
     * Crear un nuevo ejercicio
     */
    suspend fun crearEjercicio(
        diaId: Int,
        nombre: String,
        descansoSegundos: Int = 90
    ): Result<EjercicioDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = ejercicioApi.createEjercicio(
                    CreateEjercicioRequest(diaId, nombre, descansoSegundos)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error creando ejercicio: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    // ========== SERIES ==========

    /**
     * Obtener series de un ejercicio
     */
    suspend fun obtenerSeries(ejercicioId: Int): Result<List<SerieDto>> {
        return RequestQueue.execute(
            operation = {
                try {
                    val response = serieApi.getAllSeries()

                    if (response.isSuccessful && response.body() != null) {
                        val series = response.body()!!.filter { it.ejercicioId == ejercicioId }
                        Result.success(series)
                    } else {
                        Result.failure(Exception("Error obteniendo series: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            priority = RequestQueue.Priority.LOW
        )
    }

    /**
     * Crear una nueva serie
     */
    suspend fun crearSerie(
        ejercicioId: Int,
        peso: Double = 0.0,
        repeticiones: Int = 0,
        rir: Int = 0,
        completada: Boolean = false
    ): Result<SerieDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = serieApi.createSerie(
                    CreateSerieRequest(ejercicioId, peso, repeticiones, rir, completada)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error creando serie: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    /**
     * Actualizar una serie existente
     */
    suspend fun actualizarSerie(
        serieId: Int,
        peso: Double? = null,
        repeticiones: Int? = null,
        rir: Int? = null,
        completada: Boolean? = null
    ): Result<SerieDto> {
        return RequestQueue.execute(operation = {
            try {
                val response = serieApi.updateSerie(
                    serieId,
                    UpdateSerieRequest(peso, repeticiones, rir, completada)
                )

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Error actualizando serie: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    /**
     * Eliminar una serie
     */
    suspend fun eliminarSerie(serieId: Int): Result<Unit> {
        return RequestQueue.execute(operation = {
            try {
                val response = serieApi.deleteSerie(serieId)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Error eliminando serie: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        })
    }

    // ========== EJERCICIOS DISPONIBLES ==========

    /**
     * Obtener todos los ejercicios disponibles
     */
    suspend fun obtenerEjerciciosDisponibles(): Result<List<EjercicioDisponibleDto>> {
        return try {
            // Nota: Este endpoint necesita ser implementado en Xano
            // Por ahora retornamos una lista vacía
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtener ejercicios disponibles por tipo
     */
    suspend fun obtenerEjerciciosPorTipo(tipo: String): Result<List<EjercicioDisponibleDto>> {
        return try {
            // Nota: Este endpoint necesita ser implementado en Xano
            // Por ahora retornamos una lista vacía filtrando por tipo (cuando esté disponible)
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ========== DTO para Ejercicios Disponibles ==========

data class EjercicioDisponibleDto(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val descripcion: String? = null,
    val videoUrl: String? = null
)
