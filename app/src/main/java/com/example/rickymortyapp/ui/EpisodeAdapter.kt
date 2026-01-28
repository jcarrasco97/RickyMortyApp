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

class EpisodeAdapter(
    private var episodes: List<Episode>,
    private val onClick: (Episode) -> Unit,
    private val onSelectionChanged: (Boolean) -> Unit // Avisar al Fragment si hay seleccionados
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    // Lista de IDs seleccionados
    val selectedIds = HashSet<Int>()
    private var isSelectionMode = false

    class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView // El root es un CardView
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

        // Mostrar icono de ojo si ya estaba visto
        holder.ivViewed.visibility = if (episode.isViewed) View.VISIBLE else View.GONE

        // --- LÓGICA DE SELECCIÓN ---
        val isSelected = selectedIds.contains(episode.id)

        if (isSelected) {
            // Si está seleccionado: Fondo Verde suave
            holder.cardView.setCardBackgroundColor(Color.parseColor("#3300FF9C"))
            // Opcional: Borde verde
        } else {
            // Si no: Color normal (leemos del recurso card_background)
            val defaultColor = ContextCompat.getColor(holder.itemView.context, R.color.card_background)
            holder.cardView.setCardBackgroundColor(defaultColor)
        }

        // Click Normal
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(episode.id)
            } else {
                onClick(episode)
            }
        }

        // Click Largo (Activa modo selección)
        holder.itemView.setOnLongClickListener {
            toggleSelection(episode.id)
            true
        }
    }

    private fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }

        // Activar/Desactivar modo selección según si queda algo seleccionado
        isSelectionMode = selectedIds.isNotEmpty()
        onSelectionChanged(isSelectionMode) // Avisar al fragmento para mostrar/ocultar FAB

        notifyDataSetChanged() // Refrescar vista
    }

    // Función para limpiar selección después de guardar
    fun clearSelection() {
        selectedIds.clear()
        isSelectionMode = false
        onSelectionChanged(false)
        notifyDataSetChanged()
    }

    // Obtener objetos seleccionados
    fun getSelectedEpisodes(): List<Episode> {
        return episodes.filter { selectedIds.contains(it.id) }
    }

    override fun getItemCount() = episodes.size

    fun updateList(newList: List<Episode>) {
        episodes = newList
        notifyDataSetChanged()
    }
}