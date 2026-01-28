package com.example.rickymortyapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode

/**
 * Adaptador principal para el listado de episodios.
 * * Funcionalidades clave:
 * 1. Muestra la información básica del episodio.
 * 2. Gestiona el estado de "Visto" mostrando un icono de ojo.
 * 3. Implementa la lógica de SELECCIÓN MÚLTIPLE (Batch Selection):
 * - Detecta pulsación larga (onLongClick).
 * - Almacena los IDs seleccionados en un HashSet para acceso rápido.
 * - Cambia visualmente el color de fondo de los elementos seleccionados.
 */
class EpisodeAdapter(
    private var episodes: List<Episode>,
    private val onClick: (Episode) -> Unit, // Lambda para click normal (ir a detalle)
    private val onSelectionChanged: (Boolean) -> Unit // Lambda para avisar al Fragment (mostrar/ocultar FAB)
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    // Usamos un HashSet porque la búsqueda de elementos (contains) es O(1), muy eficiente.
    val selectedIds = HashSet<Int>()
    private var isSelectionMode = false

    class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val tvCode: TextView = view.findViewById(R.id.tvEpisodeCode)
        val tvName: TextView = view.findViewById(R.id.tvEpisodeName)
        val tvDate: TextView = view.findViewById(R.id.tvEpisodeDate)
        val ivViewed: ImageView = view.findViewById(R.id.ivViewed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        val episode = episodes[position]

        holder.tvCode.text = episode.episode
        holder.tvName.text = episode.name
        holder.tvDate.text = episode.airDate

        // Visibilidad del icono "ojo" según si el usuario ya vio el episodio
        holder.ivViewed.visibility = if (episode.isViewed) View.VISIBLE else View.GONE

        // --- GESTIÓN VISUAL DE LA SELECCIÓN ---
        val isSelected = selectedIds.contains(episode.id)

        if (isSelected) {
            // Feedback visual: Si está seleccionado, cambiamos el fondo a verde semitransparente.
            holder.cardView.setCardBackgroundColor(Color.parseColor("#3300FF9C"))
        } else {
            // Si no, restauramos el color original definido en los recursos (soporte tema claro/oscuro).
            val defaultColor = ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            holder.cardView.setCardBackgroundColor(defaultColor)
        }

        // Click Normal
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                // Si estamos en modo selección, el click normal sirve para marcar/desmarcar
                toggleSelection(episode.id)
            } else {
                // Si no, navegamos al detalle
                onClick(episode)
            }
        }

        // Click Largo: Inicia el modo de selección
        holder.itemView.setOnLongClickListener {
            toggleSelection(episode.id)
            true // Retornamos true para indicar que hemos consumido el evento
        }
    }

    private fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }

        // El modo selección sigue activo mientras haya al menos un elemento marcado
        isSelectionMode = selectedIds.isNotEmpty()
        onSelectionChanged(isSelectionMode)

        // Refrescamos la lista para aplicar los cambios de color
        notifyDataSetChanged()
    }

    // Limpia la selección tras guardar en Firebase
    fun clearSelection() {
        selectedIds.clear()
        isSelectionMode = false
        onSelectionChanged(false)
        notifyDataSetChanged()
    }

    // Devuelve la lista de objetos Episodio seleccionados para procesarlos en el Fragment
    fun getSelectedEpisodes(): List<Episode> {
        return episodes.filter { selectedIds.contains(it.id) }
    }

    override fun getItemCount() = episodes.size

    fun updateList(newList: List<Episode>) {
        episodes = newList
        notifyDataSetChanged()
    }
}