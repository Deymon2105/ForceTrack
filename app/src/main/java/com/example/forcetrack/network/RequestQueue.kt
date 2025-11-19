package com.example.forcetrack.network

import android.util.Log
import com.example.forcetrack.config.ApiConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

/**
 * Sistema de cola inteligente para peticiones a Xano
 * Evita error 429 (Too Many Requests) controlando el flujo de peticiones
 * VERSIÓN MEJORADA con reintentos automáticos y exponential backoff
 * Ahora usa configuración centralizada de ApiConfig
 */
object RequestQueue {
    private const val TAG = "RequestQueue"

    // Configuración del rate limiting desde ApiConfig
    private val MIN_DELAY_BETWEEN_REQUESTS = ApiConfig.RateLimiting.MIN_DELAY_BETWEEN_REQUESTS_MS
    private val MAX_REQUESTS_PER_SECOND = ApiConfig.RateLimiting.MAX_REQUESTS_PER_SECOND
    private val BACKOFF_ON_429 = ApiConfig.RateLimiting.BACKOFF_ON_429_MS
    private val MAX_RETRIES = ApiConfig.RateLimiting.MAX_RETRIES

    private val mutex = Mutex()
    private var lastRequestTime = 0L
    private var requestCount = 0
    private var windowStartTime = 0L
    private var consecutive429Count = 0

    /**
     * Ejecuta una petición con control de rate limiting automático y reintentos
     */
    suspend fun <T> execute(
        operation: suspend () -> Result<T>,
        priority: Priority = Priority.NORMAL,
        retryCount: Int = 0
    ): Result<T> {
        return mutex.withLock {
            // Si hay muchos 429 consecutivos, esperar más tiempo
            if (consecutive429Count > 2) {
                val extraWait = consecutive429Count * 2000L
                Log.w(TAG, "Detectados $consecutive429Count errores 429 consecutivos. Esperando ${extraWait}ms extra")
                delay(extraWait)
            }

            // Esperar si es necesario para no exceder el rate limit
            waitIfNeeded()

            // Ejecutar la petición
            val result = try {
                operation()
            } catch (e: Exception) {
                Log.e(TAG, "Error en petición: ${e.message}")
                Result.failure(e)
            }

            // Actualizar contadores
            updateCounters()

            // Manejar errores y reintentos
            result.onFailure { error ->
                val is429Error = error.message?.contains("429") == true ||
                                 (error is HttpException && error.code() == 429)

                if (is429Error && retryCount < MAX_RETRIES) {
                    consecutive429Count++
                    // Exponential backoff: esperar más con cada reintento
                    val backoffDelay = BACKOFF_ON_429 * (retryCount + 1)
                    Log.w(TAG, "Error 429 detectado (intento ${retryCount + 1}/$MAX_RETRIES). Esperando ${backoffDelay}ms antes de reintentar...")
                    delay(backoffDelay)

                    // Reintentar la operación
                    return execute(operation, priority, retryCount + 1)
                } else if (is429Error) {
                    Log.e(TAG, "Error 429 persistente después de $MAX_RETRIES reintentos")
                    consecutive429Count++
                } else {
                    // Si no es error 429, resetear contador
                    consecutive429Count = 0
                }
            }

            result.onSuccess {
                // Si la petición fue exitosa, resetear contador de errores 429
                consecutive429Count = 0
            }

            result
        }
    }

    private suspend fun waitIfNeeded() {
        val now = System.currentTimeMillis()

        // Resetear ventana si ha pasado 1 segundo
        if (now - windowStartTime >= 1000) {
            windowStartTime = now
            requestCount = 0
        }

        // Si ya hicimos muchas peticiones en esta ventana, esperar
        if (requestCount >= MAX_REQUESTS_PER_SECOND) {
            val waitTime = 1000 - (now - windowStartTime)
            if (waitTime > 0) {
                Log.d(TAG, "Rate limit alcanzado. Esperando ${waitTime}ms...")
                delay(waitTime)
                windowStartTime = System.currentTimeMillis()
                requestCount = 0
            }
        }

        // Esperar el delay mínimo entre peticiones
        val timeSinceLastRequest = now - lastRequestTime
        if (timeSinceLastRequest < MIN_DELAY_BETWEEN_REQUESTS) {
            val waitTime = MIN_DELAY_BETWEEN_REQUESTS - timeSinceLastRequest
            Log.d(TAG, "Delay de ${waitTime}ms entre peticiones")
            delay(waitTime)
        }
    }

    private fun updateCounters() {
        lastRequestTime = System.currentTimeMillis()
        requestCount++

        Log.d(TAG, "Peticiones en ventana actual: $requestCount/$MAX_REQUESTS_PER_SECOND")
    }

    /**
     * Resetea manualmente el estado del RequestQueue
     * Útil si necesitas reiniciar el sistema anti-spam
     */
    fun reset() {
        lastRequestTime = 0L
        requestCount = 0
        windowStartTime = 0L
        consecutive429Count = 0
        Log.i(TAG, "RequestQueue reseteado")
    }

    enum class Priority {
        HIGH,    // Peticiones críticas (login, registro)
        NORMAL,  // Peticiones normales (crear, actualizar)
        LOW      // Peticiones de menor prioridad (cargar listas)
    }
}
