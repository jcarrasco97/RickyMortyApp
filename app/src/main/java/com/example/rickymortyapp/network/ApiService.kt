package com.example.rickymortyapp.network

import com.example.rickymortyapp.models.EpisodeResponse
import com.example.rickymortyapp.models.Character
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interfaz que define los endpoints de la API REST de Rick and Morty.
 * Retrofit utiliza esta interfaz para generar el código de red automáticamente.
 */
interface ApiService {

    // 1. Obtener listado de episodios.
    // Usamos @Query("page") para añadir parámetros a la URL.
    // Resultado: https://rickandmortyapi.com/api/episode?page=1
    @GET("episode")
    fun getEpisodes(@Query("page") page: Int): Call<EpisodeResponse>

    // 2. Obtener múltiples personajes específicos.
    // Usamos @Path("ids") para sustituir dinámicamente parte de la URL.
    // Esto es crucial para la pantalla de detalle, donde pedimos solo los personajes del episodio.
    // Resultado: https://rickandmortyapi.com/api/character/1,2,3,4
    @GET("character/{ids}")
    fun getMultipleCharacters(@Path("ids") ids: String): Call<List<Character>>
}