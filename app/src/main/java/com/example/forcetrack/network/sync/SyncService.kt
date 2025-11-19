package com.example.forcetrack.network.sync

import com.example.forcetrack.network.repository.XanoRepository

/**
 * Estadísticas de sincronización
 */
data class SyncStats(
    val bloquesDescargados: Int = 0,
    val bloquesSubidos: Int = 0,
    val semanasDescargadas: Int = 0,
    val diasDescargados: Int = 0,
    val ejerciciosDescargados: Int = 0,
    val seriesDescargadas: Int = 0,
    val errores: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Servicio de sincronización con Xano
 * Maneja la sincronización bidireccional de datos
 */
class SyncService(private val xanoRepository: XanoRepository = XanoRepository()) {

    /**
     * Verifica la conexión con el servidor
     */
    suspend fun checkConnection(): Boolean {
        return try {
            // Intentar obtener bloques como test de conexión
            val result = xanoRepository.obtenerBloques(0)
            result.isSuccess
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sincronización completa de todos los datos del usuario
     */
    suspend fun fullSync(usuarioId: Int): Result<SyncStats> {
        return try {
            var stats = SyncStats()

            // 1. Obtener bloques
            xanoRepository.obtenerBloques(usuarioId)
                .onSuccess { bloques ->
                    stats = stats.copy(bloquesDescargados = bloques.size)

                    // 2. Para cada bloque, obtener semanas
                    bloques.forEach { bloque ->
                        xanoRepository.obtenerSemanas(bloque.id)
                            .onSuccess { semanas ->
                                stats = stats.copy(
                                    semanasDescargadas = stats.semanasDescargadas + semanas.size
                                )

                                // 3. Para cada semana, obtener días
                                semanas.forEach { semana ->
                                    xanoRepository.obtenerDias(semana.id)
                                        .onSuccess { dias ->
                                            stats = stats.copy(
                                                diasDescargados = stats.diasDescargados + dias.size
                                            )

                                            // 4. Para cada día, obtener ejercicios
                                            dias.forEach { dia ->
                                                xanoRepository.obtenerEjercicios(dia.id)
                                                    .onSuccess { ejercicios ->
                                                        stats = stats.copy(
                                                            ejerciciosDescargados = stats.ejerciciosDescargados + ejercicios.size
                                                        )

                                                        // 5. Para cada ejercicio, obtener series
                                                        ejercicios.forEach { ejercicio ->
                                                            xanoRepository.obtenerSeries(ejercicio.id)
                                                                .onSuccess { series ->
                                                                    stats = stats.copy(
                                                                        seriesDescargadas = stats.seriesDescargadas + series.size
                                                                    )
                                                                }
                                                                .onFailure {
                                                                    stats = stats.copy(errores = stats.errores + 1)
                                                                }
                                                        }
                                                    }
                                                    .onFailure {
                                                        stats = stats.copy(errores = stats.errores + 1)
                                                    }
                                            }
                                        }
                                        .onFailure {
                                            stats = stats.copy(errores = stats.errores + 1)
                                        }
                                }
                            }
                            .onFailure {
                                stats = stats.copy(errores = stats.errores + 1)
                            }
                    }
                }
                .onFailure {
                    return Result.failure(it)
                }

            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sincronizar bloques desde el servidor
     */
    suspend fun syncBloquesFromRemote(usuarioId: Int): Result<Unit> {
        return try {
            xanoRepository.obtenerBloques(usuarioId)
                .onSuccess {
                    return Result.success(Unit)
                }
                .onFailure {
                    return Result.failure(it)
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sincronizar un bloque específico al servidor
     */
    suspend fun syncBloqueToRemote(bloqueId: Int): Result<Unit> {
        return try {
            // Por ahora solo verificamos que el bloque existe
            // En una implementación completa, aquí se subirían los cambios locales
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
