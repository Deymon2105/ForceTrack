package com.example.forcetrack.network.sync

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Estadísticas básicas de sincronización
 */
data class SyncStats(
    val bloquesDescargados: Int = 0,
    val bloquesSubidos: Int = 0,
    val seriesSincronizadas: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Servicio de sincronización temporal.
 * TODO: Reemplazar con implementación real contra el backend de Xano.
 */
class SyncService {

    /**
     * Verifica conectividad rudimentaria.
     */
    suspend fun checkConnection(): Boolean {
        delay(50.milliseconds)
        return true
    }

    /**
     * Sincroniza todo el contenido del usuario.
     */
    suspend fun fullSync(usuarioId: Int): Result<SyncStats> {
        delay(150.milliseconds)
        return Result.success(SyncStats(bloquesDescargados = 1, bloquesSubidos = 1))
    }

    /**
     * Descarga bloques públicos/propios del backend.
     */
    suspend fun syncBloquesFromRemote(usuarioId: Int): Result<Unit> {
        delay(100.milliseconds)
        return Result.success(Unit)
    }

    /**
     * Envía un bloque individual al backend.
     */
    suspend fun syncBloqueToRemote(bloqueId: Int): Result<Unit> {
        delay(100.milliseconds)
        return Result.success(Unit)
    }
}

