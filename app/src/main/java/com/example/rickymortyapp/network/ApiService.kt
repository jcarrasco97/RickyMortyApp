package com.example.rickymortyapp.network

import com.example.rickymortyapp.models.EpisodeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("episode") // El endpoint de la API
    fun getEpisodes(@Query("page") page: Int): Call<EpisodeResponse>
}