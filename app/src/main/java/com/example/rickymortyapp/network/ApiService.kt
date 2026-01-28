package com.example.rickymortyapp.network

import com.example.rickymortyapp.models.EpisodeResponse
import com.example.rickymortyapp.models.Character // Importamos TU modelo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Obtener episodios (paginado)
    @GET("episode")
    fun getEpisodes(@Query("page") page: Int): Call<EpisodeResponse>

    // --- ESTA ES LA QUE TE FALTABA O DABA ERROR ---
    // Obtener múltiples personajes por ID (ej: "1,2,3")
    @GET("character/{ids}")
    fun getMultipleCharacters(@Path("ids") ids: String): Call<List<Character>>
}