package com.example.forcetrack.config

object ApiConfig {

    /**
     * URLs base del Backend Local (Spring Boot)
     * El backend actúa como intermediario con Supabase
     */
    private object LocalEndpoints {
        // 10.0.2.2 es el alias de localhost para el emulador de Android
        // Si usas un dispositivo físico, cambia esto por la IP de tu PC (ej. 192.168.1.X)
        // 10.0.2.2 es el alias de localhost para el emulador de Android
        // Para ejecutar en el emulador usa esta URL. Si pruebas en un dispositivo físico, reemplaza por la IP de tu PC.
        const val BASE_URL = "http://10.0.2.2:8080/api/"  // Emulador
    }

    /**
     * URL de Producción (Cuando despliegues el backend)
     * Cambia esto por la URL real que te de tu proveedor (Railway, Render, etc.)
     */
    private const val PROD_URL = "https://backendforcetrack-production.up.railway.app/api/"

    /**
     * Cambia a TRUE cuando vayas a generar el APK para Play Store
     */
    private const val IS_PRODUCTION = true

    /**
     * Getters públicos
     */
    fun getBaseUrl(): String = if (IS_PRODUCTION) PROD_URL else LocalEndpoints.BASE_URL
    
    // Mantener compatibilidad con el código existente que usa estos getters
    fun getAuthUrl(): String = getBaseUrl()
    fun getUsuarioUrl(): String = getBaseUrl()
    fun getBloqueUrl(): String = getBaseUrl()
    fun getDiaUrl(): String = getBaseUrl()
    fun getEjercicioUrl(): String = getBaseUrl()
    fun getSerieUrl(): String = getBaseUrl()
    fun getEjercicioDisponibleUrl(): String = getBaseUrl()
    fun getTrainingLogUrl(): String = getBaseUrl()

    /**
     * Configuración de timeouts
     */
    object NetworkTimeouts {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }
}

