package com.example.forcetrack.network

import com.example.forcetrack.config.ApiConfig

/**
 * Configuración de Xano para ForceTrack
 * ⚠️ DEPRECADO: Usar ApiConfig en su lugar
 * Este objeto se mantiene por compatibilidad pero redirige a ApiConfig
 */
@Deprecated(
    message = "Usar ApiConfig en lugar de XanoConfig para mejor seguridad",
    replaceWith = ReplaceWith("ApiConfig", "com.example.forcetrack.config.ApiConfig")
)
object XanoConfig {

    /**
     * URLs BASE DE XANO - Ahora delegadas a ApiConfig
     */
    @Deprecated("Usar ApiConfig.getAuthUrl()")
    const val XANO_BASE_URL_AUTH = "https://x8ki-letl-twmt.n7.xano.io/api:1WBxk3LL/"

    @Deprecated("Usar ApiConfig.getUsuarioUrl()")
    const val XANO_BASE_URL_USUARIO = "https://x8ki-letl-twmt.n7.xano.io/api:BL46WclC/"

    @Deprecated("Usar ApiConfig.getBloqueUrl()")
    const val XANO_BASE_URL_BLOQUE = "https://x8ki-letl-twmt.n7.xano.io/api:GG8vMuYP/"

    @Deprecated("Usar ApiConfig.getSemanaUrl()")
    const val XANO_BASE_URL_SEMANA = "https://x8ki-letl-twmt.n7.xano.io/api:qcDvOmLj/"

    @Deprecated("Usar ApiConfig.getDiaUrl()")
    const val XANO_BASE_URL_DIA = "https://x8ki-letl-twmt.n7.xano.io/api:fwvjHhq7/"

    @Deprecated("Usar ApiConfig.getEjercicioUrl()")
    const val XANO_BASE_URL_EJERCICIO = "https://x8ki-letl-twmt.n7.xano.io/api:gGeaMRl3/"

    @Deprecated("Usar ApiConfig.getSerieUrl()")
    const val XANO_BASE_URL_SERIE = "https://x8ki-letl-twmt.n7.xano.io/api:8upbkWdF/"

    @Deprecated("Usar ApiConfig.getEjercicioDisponibleUrl()")
    const val XANO_BASE_URL_EJER_DISPO = "https://x8ki-letl-twmt.n7.xano.io/api:u6oZGONG/"

    @Deprecated("Usar ApiConfig.getTrainingLogUrl()")
    const val XANO_BASE_URL_TRAINING_LOG = "https://x8ki-letl-twmt.n7.xano.io/api:Q3_QTtDt/"

    /**
     * Verificar si las URLs están configuradas
     */
    fun isConfigured(): Boolean = ApiConfig.isConfigured()

    /**
     * Obtener mensaje de error si no está configurado
     */
    fun getErrorMessage(): String {
        return if (isConfigured()) {
            ""
        } else {
            "⚠️ Xano no está configurado. Verifica ApiConfig.kt"
        }
    }
}
