package com.example.rickymortyapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton (solo existe una instancia en toda la app) encargado de
 * configurar y proveer el cliente de Retrofit.
 */
object RetrofitClient {

    // URL base de la API. Todas las peticiones de ApiService se concatenan a esto.
    private const val BASE_URL = "https://rickandmortyapi.com/api/"

    /**
     * Instancia de ApiService creada perezosamente (by lazy).
     * * 1. Se inicializa solo la primera vez que se llama a 'apiService', ahorrando recursos.
     * 2. Configura GsonConverterFactory para transformar automáticamente el JSON de la API
     * en nuestras Data Classes de Kotlin (Parsing automático).
     */
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}