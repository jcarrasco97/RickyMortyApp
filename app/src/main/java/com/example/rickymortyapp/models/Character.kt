package com.example.rickymortyapp.models

/**
 * Modelo de datos que representa a un Personaje de la serie.
 * * Se utiliza principalmente en la vista de Detalle del Episodio, donde cargamos
 * una lista de personajes a partir de sus IDs.
 * * Nota: Solo mapeamos las propiedades que necesitamos para la UI (nombre y foto),
 * ignorando el resto de datos que devuelve la API para ahorrar memoria.
 */
data class Character(
    val id: Int,
    val name: String,
    val image: String // URL de la imagen que cargaremos posteriormente con Glide
)