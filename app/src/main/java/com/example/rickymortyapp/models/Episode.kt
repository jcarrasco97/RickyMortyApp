package com.example.rickymortyapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Modelo principal que representa un Episodio.
 *
 * Implementa la interfaz [Parcelable] mediante la anotación @Parcelize.
 * Esto permite "serializar" el objeto completo para pasarlo desde la lista (EpisodeListFragment)
 * al detalle (EpisodeDetailFragment) a través de los argumentos de navegación (Bundle),
 * evitando tener que volver a pedir los datos a la API.
 */
@Parcelize
data class Episode(
    val id: Int,
    val name: String,

    // @SerializedName nos permite mapear el campo JSON "air_date" (snake_case)
    // a nuestra variable Kotlin "airDate" (camelCase) siguiendo las convenciones de código.
    @SerializedName("air_date") val airDate: String,

    val episode: String, // Código del episodio (ej: S01E01)

    // Lista de URLs de los personajes que aparecen en este episodio.
    // La API devuelve urls tipo: "https://.../character/1", de donde extraeremos el ID.
    val characters: List<String>,

    val url: String,

    // Propiedad local (no viene de la API).
    // La usamos para gestionar el estado visual del "ojo" (Visto/No visto).
    // Es mutable (var) porque cambiará durante la ejecución de la app al interactuar con Firebase.
    var isViewed: Boolean = false
) : Parcelable