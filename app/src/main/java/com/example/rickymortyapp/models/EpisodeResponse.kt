package com.example.rickymortyapp.models

/**
 * Clase contenedora (Wrapper) para la respuesta de la API.
 * * La API de Rick y Morty utiliza paginación. La respuesta JSON tiene dos objetos raíz:
 * 1. "info": Metadatos sobre la paginación.
 * 2. "results": La lista real de episodios.
 */
data class EpisodeResponse(
    val info: Info,
    val results: List<Episode>
)

/**
 * Metadatos de la paginación.
 * Nos sirve para saber cuántos episodios totales existen o si hay una página siguiente.
 */
data class Info(
    val count: Int,      // Total de episodios disponibles en la API
    val pages: Int,      // Total de páginas
    val next: String?,   // URL de la siguiente página (null si es la última)
    val prev: String?    // URL de la página anterior
)