package com.example.forcetrack.config

/**
 * 🔐 Configuración segura de endpoints de Xano
 *
 * IMPORTANTE: En producción, estas URLs deberían estar en:
 * 1. Variables de entorno
 * 2. Archivo local.properties (no versionado en Git)
 * 3. Sistema de gestión de secretos (como Google Cloud Secret Manager)
 *
 * Para mayor seguridad, considera:
 * - Añadir este archivo a .gitignore
 * - Usar BuildConfig para diferentes entornos (dev/prod)
 * - Implementar ofuscación de código con ProGuard/R8
 */
object ApiConfig {

    /**
     * URLs base de Xano por grupo de API
     * Cada grupo de endpoints tiene su propia URL
     */
    private object XanoEndpoints {
        const val AUTH = "https://x8ki-letl-twmt.n7.xano.io/api:1WBxk3LL/"
        const val USUARIO = "https://x8ki-letl-twmt.n7.xano.io/api:BL46WclC/"
        const val BLOQUE = "https://x8ki-letl-twmt.n7.xano.io/api:GG8vMuYP/"
        const val SEMANA = "https://x8ki-letl-twmt.n7.xano.io/api:qcDvOmLj/"
        const val DIA = "https://x8ki-letl-twmt.n7.xano.io/api:fwvjHhq7/"
        const val EJERCICIO = "https://x8ki-letl-twmt.n7.xano.io/api:gGeaMRl3/"
        const val SERIE = "https://x8ki-letl-twmt.n7.xano.io/api:8upbkWdF/"
        const val EJERCICIO_DISPONIBLE = "https://x8ki-letl-twmt.n7.xano.io/api:u6oZGONG/"
        const val TRAINING_LOG = "https://x8ki-letl-twmt.n7.xano.io/api:Q3_QTtDt/"
    }

    /**
     * Getters públicos (solo lectura)
     * Las URLs se acceden mediante métodos, no constantes públicas
     */
    fun getAuthUrl(): String = XanoEndpoints.AUTH
    fun getUsuarioUrl(): String = XanoEndpoints.USUARIO
    fun getBloqueUrl(): String = XanoEndpoints.BLOQUE
    fun getSemanaUrl(): String = XanoEndpoints.SEMANA
    fun getDiaUrl(): String = XanoEndpoints.DIA
    fun getEjercicioUrl(): String = XanoEndpoints.EJERCICIO
    fun getSerieUrl(): String = XanoEndpoints.SERIE
    fun getEjercicioDisponibleUrl(): String = XanoEndpoints.EJERCICIO_DISPONIBLE
    fun getTrainingLogUrl(): String = XanoEndpoints.TRAINING_LOG

    /**
     * Configuración de timeouts
     */
    object NetworkTimeouts {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }

    /**
     * Configuración de rate limiting
     */
    object RateLimiting {
        const val MIN_DELAY_BETWEEN_REQUESTS_MS = 800L
        const val MAX_REQUESTS_PER_SECOND = 2
        const val BACKOFF_ON_429_MS = 3000L
        const val MAX_RETRIES = 3
    }

    /**
     * Verifica si las URLs están configuradas correctamente
     */
    fun isConfigured(): Boolean {
        return XanoEndpoints.AUTH.isNotEmpty() &&
               XanoEndpoints.AUTH.startsWith("https://")
    }

    /**
     * Obtiene información de configuración (sin exponer URLs completas)
     */
    fun getConfigInfo(): String {
        val domain = XanoEndpoints.AUTH.substringAfter("https://").substringBefore("/")
        return "Xano Backend: $domain (${if (isConfigured()) "✅ Configurado" else "❌ No configurado"})"
    }
}

