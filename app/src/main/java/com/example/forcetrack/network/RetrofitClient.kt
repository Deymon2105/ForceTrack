package com.example.forcetrack.network

import com.example.forcetrack.config.ApiConfig
import com.example.forcetrack.network.api.XanoApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de Retrofit para comunicación con la API de Xano
 * Cada grupo de endpoints tiene su propia instancia
 * ✨ ACTUALIZADO: Ahora usa ApiConfig para mayor seguridad
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ApiConfig.NetworkTimeouts.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(ApiConfig.NetworkTimeouts.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(ApiConfig.NetworkTimeouts.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ========== APIs POR GRUPO ==========

    /**
     * API de Usuarios
     */
    val usuarioApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getUsuarioUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Bloques
     */
    val bloqueApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getBloqueUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Semanas
     */
    val semanaApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getSemanaUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Días
     */
    val diaApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getDiaUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Ejercicios
     */
    val ejercicioApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getEjercicioUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Series
     */
    val serieApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getSerieUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Ejercicios Disponibles
     */
    val ejercicioDisponibleApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getEjercicioDisponibleUrl()).create(XanoApi::class.java)
    }

    /**
     * API de Training Logs
     */
    val trainingLogApi: XanoApi by lazy {
        createRetrofit(ApiConfig.getTrainingLogUrl()).create(XanoApi::class.java)
    }
}
