package com.example.forcetrack.network.repository

import com.example.forcetrack.network.dto.*
import com.example.forcetrack.network.api.UsuarioDto as XanoUsuarioDto
import com.example.forcetrack.network.api.BloqueDto as XanoBloqueDto
import com.example.forcetrack.network.api.DiaDto as XanoDiaDto
import com.example.forcetrack.network.api.EjercicioDto as XanoEjercicioDto
import com.example.forcetrack.network.api.SerieDto as XanoSerieDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Repositorio para manejar las operaciones con la API REST
 * Convierte entre los DTOs de Xano y los DTOs del dominio
 */
class RemoteRepository {

    private val xanoRepo = XanoRepository()

    // ========== AUTENTICACIÓN ==========

    suspend fun login(correo: String, contrasena: String): Result<AuthResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.login(correo, contrasena)
                    .onSuccess { xanoUsuario ->
                        val authResponse = AuthResponseDto(
                            success = true,
                            message = "Login exitoso",
                            usuario = UsuarioDto(
                                id = xanoUsuario.id,
                                nombreUsuario = xanoUsuario.nombreUsuario,
                                correo = xanoUsuario.correo,
                                contrasena = null
                            ),
                            token = null
                        )
                        return@withContext Result.success(authResponse)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(nombreUsuario: String, correo: String, contrasena: String): Result<AuthResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("RemoteRepository", "📝 REGISTRO - Datos a enviar:")
                Log.d("RemoteRepository", "   nombreUsuario: $nombreUsuario")
                Log.d("RemoteRepository", "   correo: $correo")
                Log.d("RemoteRepository", "   contrasena: $contrasena")

                xanoRepo.register(nombreUsuario, correo, contrasena)
                    .onSuccess { xanoUsuario ->
                        Log.d("RemoteRepository", "✅ Usuario recibido desde Xano:")
                        Log.d("RemoteRepository", "   id: ${xanoUsuario.id}")
                        Log.d("RemoteRepository", "   nombreUsuario: ${xanoUsuario.nombreUsuario}")
                        Log.d("RemoteRepository", "   correo: ${xanoUsuario.correo}")

                        val authResponse = AuthResponseDto(
                            success = true,
                            message = "Registro exitoso",
                            usuario = UsuarioDto(
                                id = xanoUsuario.id,
                                nombreUsuario = xanoUsuario.nombreUsuario,
                                correo = xanoUsuario.correo,
                                contrasena = null
                            ),
                            token = null
                        )
                        return@withContext Result.success(authResponse)
                    }
                    .onFailure { error ->
                        Log.e("RemoteRepository", "❌ Error en registro:")
                        Log.e("RemoteRepository", "   Mensaje: ${error.message}")
                        error.printStackTrace()
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Log.e("RemoteRepository", "❌ Excepción en registro: ${e.message}")
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    suspend fun userExists(username: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            Result.success(false)
        }
    }

    // ========== BLOQUES ==========

    suspend fun getBloques(usuarioId: Int): Result<List<BloqueDto>> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.obtenerBloques(usuarioId)
                    .onSuccess { xanoBloques ->
                        val bloques = xanoBloques.map { xanoBloque ->
                            BloqueDto(
                                id = xanoBloque.id,
                                usuarioId = xanoBloque.usuarioId,
                                nombre = xanoBloque.nombre,
                                dias = emptyList()
                            )
                        }
                        return@withContext Result.success(bloques)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getBloque(bloqueId: Int): Result<BloqueDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Obtener bloques y filtrar el que necesitamos
                val bloquesResult = xanoRepo.obtenerBloques(0) // Obtener todos
                val diasResult = xanoRepo.obtenerDias(bloqueId)

                bloquesResult.onSuccess { bloques ->
                    val bloque = bloques.firstOrNull { it.id == bloqueId }
                    if (bloque != null) {
                        diasResult.onSuccess { xanoDias ->
                            val bloqueDto = BloqueDto(
                                id = bloque.id,
                                usuarioId = bloque.usuarioId,
                                nombre = bloque.nombre,
                                dias = xanoDias.map { xanoDia ->
                                    DiaDto(
                                        id = xanoDia.id,
                                        bloqueId = xanoDia.bloqueId,
                                        nombre = xanoDia.nombre,
                                        notas = xanoDia.notas,
                                        ejercicios = emptyList()
                                    )
                                }
                            )
                            return@withContext Result.success(bloqueDto)
                        }
                    }
                }
                Result.failure(Exception("Bloque no encontrado"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createBloque(bloque: BloqueDto): Result<BloqueDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.crearBloque(bloque.usuarioId, bloque.nombre)
                    .onSuccess { xanoBloque ->
                        val bloqueDto = BloqueDto(
                            id = xanoBloque.id,
                            usuarioId = xanoBloque.usuarioId,
                            nombre = xanoBloque.nombre,
                            dias = emptyList()
                        )
                        return@withContext Result.success(bloqueDto)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteBloque(bloqueId: Int): Result<Unit> {
        return xanoRepo.eliminarBloque(bloqueId)
    }

    // ========== DÍAS ==========

    suspend fun createDia(dia: DiaDto): Result<DiaDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.crearDia(dia.bloqueId, dia.nombre, dia.notas)
                    .onSuccess { xanoDia ->
                        val diaDto = DiaDto(
                            id = xanoDia.id,
                            bloqueId = xanoDia.bloqueId,
                            nombre = xanoDia.nombre,
                            notas = xanoDia.notas,
                            ejercicios = emptyList()
                        )
                        return@withContext Result.success(diaDto)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateNotasDia(diaId: Int, notas: String): Result<Unit> {
        return Result.success(Unit)
    }

    // ========== EJERCICIOS ==========

    suspend fun createEjercicio(ejercicio: EjercicioDto): Result<EjercicioDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.crearEjercicio(
                    ejercicio.diaId,
                    ejercicio.nombre,
                    ejercicio.descansoSegundos
                )
                    .onSuccess { xanoEjercicio ->
                        val ejercicioDto = EjercicioDto(
                            id = xanoEjercicio.id,
                            diaId = xanoEjercicio.diaId,
                            nombre = xanoEjercicio.nombre,
                            descansoSegundos = xanoEjercicio.descansoSegundos,
                            series = emptyList()
                        )
                        return@withContext Result.success(ejercicioDto)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteEjercicio(ejercicioId: Int): Result<Unit> {
        return Result.success(Unit)
    }

    // ========== SERIES ==========

    suspend fun createSerie(serie: SerieDto): Result<SerieDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.crearSerie(
                    serie.ejercicioId,
                    serie.peso,
                    serie.repeticiones,
                    serie.rir,
                    serie.completada
                )
                    .onSuccess { xanoSerie ->
                        val serieDto = SerieDto(
                            id = xanoSerie.id,
                            ejercicioId = xanoSerie.ejercicioId,
                            peso = xanoSerie.peso,
                            repeticiones = xanoSerie.repeticiones,
                            rir = xanoSerie.rir,
                            completada = xanoSerie.completada
                        )
                        return@withContext Result.success(serieDto)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateSerie(serieId: Int, serie: SerieDto): Result<SerieDto> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.actualizarSerie(
                    serieId,
                    serie.peso,
                    serie.repeticiones,
                    serie.rir,
                    serie.completada
                )
                    .onSuccess { xanoSerie ->
                        val serieDto = SerieDto(
                            id = xanoSerie.id,
                            ejercicioId = xanoSerie.ejercicioId,
                            peso = xanoSerie.peso,
                            repeticiones = xanoSerie.repeticiones,
                            rir = xanoSerie.rir,
                            completada = xanoSerie.completada
                        )
                        return@withContext Result.success(serieDto)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSerie(serieId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                xanoRepo.eliminarSerie(serieId)
                    .onSuccess {
                        return@withContext Result.success(Unit)
                    }
                    .onFailure { error ->
                        return@withContext Result.failure(error)
                    }
                Result.failure(Exception("Error desconocido"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ========== SINCRONIZACIÓN ==========

    suspend fun syncData(syncRequest: SyncRequestDto): Result<SyncResponseDto> {
        return Result.success(SyncResponseDto(success = true, timestamp = System.currentTimeMillis()))
    }

    suspend fun getFullSync(usuarioId: Int): Result<SyncResponseDto> {
        return Result.success(SyncResponseDto(success = true, timestamp = System.currentTimeMillis()))
    }
}
