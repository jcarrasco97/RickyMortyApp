package com.example.rickymortyapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode

class EpisodeAdapter(
    private var episodes: List<Episode>,
    private val onClick: (Episode) -> Unit // Función lambda para saber cuándo hacen click
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    class EpisodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCode: TextView = view.findViewById(R.id.tvEpisodeCode)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvName: TextView = view.findViewById(R.id.tvEpisodeName)
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
        holder.tvDate.text = episode.airDate
        holder.tvName.text = episode.name

        // Configuración del icono de "visto" (Lógica futura)
        if (episode.isViewed) {
            holder.ivViewed.visibility = View.VISIBLE
        } else {
            holder.ivViewed.visibility = View.GONE
        }

        // Click en toda la tarjeta
        holder.itemView.setOnClickListener {
            onClick(episode)
        }
    }

    override fun getItemCount() = episodes.size

    // Función para actualizar la lista cuando lleguen datos de internet
    fun updateList(newList: List<Episode>) {
        episodes = newList
        notifyDataSetChanged()
    }
}