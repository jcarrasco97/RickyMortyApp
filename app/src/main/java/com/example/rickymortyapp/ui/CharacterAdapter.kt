package com.example.rickymortyapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Character

/**
 * Adaptador para el RecyclerView de Personajes.
 * Se encarga de inflar el diseño de cada celda (item_character) y vincular los datos
 * (nombre e imagen) utilizando la librería Glide para la carga asíncrona de imágenes.
 */
class CharacterAdapter(private var characters: List<Character>) :
    RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    class CharacterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivCharacter)
        val name: TextView = view.findViewById(R.id.tvCharacterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val char = characters[position]
        holder.name.text = char.name

        // Usamos Glide para cargar la URL de la imagen en el ImageView.
        // Glide gestiona automáticamente la caché y el redimensionado para no bloquear la UI.
        Glide.with(holder.itemView.context)
            .load(char.image)
            .into(holder.image)
    }

    override fun getItemCount() = characters.size

    fun updateList(newList: List<Character>) {
        characters = newList
        // Notificamos al adaptador que los datos han cambiado para que repinte la lista.
        notifyDataSetChanged()
    }
}