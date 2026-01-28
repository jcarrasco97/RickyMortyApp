package com.example.rickymortyapp.models

import android.os.Parcelable // Importar esto
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize // Importar esto

@Parcelize // 1. La anotación mágica
data class Episode(
    val id: Int,
    val name: String,
    @SerializedName("air_date") val airDate: String,
    val episode: String,
    val characters: List<String>,
    val url: String,
    var isViewed: Boolean = false
) : Parcelable // 2. La herencia