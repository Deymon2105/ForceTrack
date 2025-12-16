package com.example.forcetrack.network

import com.example.forcetrack.config.ApiConfig
import com.example.forcetrack.network.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente de Retrofit para comunicación con la API REST del backend Spring Boot
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
     * API de Autenticación (Login/Signup)
     */
    val authApi: ApiService by lazy {
        createRetrofit(ApiConfig.getAuthUrl()).create(ApiService::class.java)
    }

    /**
     * API de Usuarios
     */
    val usuarioApi: ApiService by lazy {
        createRetrofit(ApiConfig.getUsuarioUrl()).create(ApiService::class.java)
    }

    /**
     * API de Bloques
     */
    val bloqueApi: ApiService by lazy {
        createRetrofit(ApiConfig.getBloqueUrl()).create(ApiService::class.java)
    }

    /**
     * API de Días
     */
    val diaApi: ApiService by lazy {
        createRetrofit(ApiConfig.getDiaUrl()).create(ApiService::class.java)
    }

    /**
     * API de Ejercicios
     */
    val ejercicioApi: ApiService by lazy {
        createRetrofit(ApiConfig.getEjercicioUrl()).create(ApiService::class.java)
    }

    /**
     * API de Series
     */
    val serieApi: ApiService by lazy {
        createRetrofit(ApiConfig.getSerieUrl()).create(ApiService::class.java)
    }

    /**
     * API de Ejercicios Disponibles
     */
    val ejercicioDisponibleApi: ApiService by lazy {
        createRetrofit(ApiConfig.getEjercicioDisponibleUrl()).create(ApiService::class.java)
    }

    /**
     * API de Training Logs
     */
    val trainingLogApi: ApiService by lazy {
        createRetrofit(ApiConfig.getTrainingLogUrl()).create(ApiService::class.java)
    }
}
