package com.example.rickymortyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EpisodeDetailFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvDate: TextView
    private lateinit var switchViewed: SwitchMaterial

    // Instancias de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_episode_detail, container, false)

        tvName = view.findViewById(R.id.tvDetailName)
        tvCode = view.findViewById(R.id.tvDetailCode)
        tvDate = view.findViewById(R.id.tvDetailDate)
        switchViewed = view.findViewById(R.id.switchViewed)

        // Recuperar el episodio que nos pasó la lista
        val episode = arguments?.getParcelable<Episode>("episode_data")

        if (episode != null) {
            setupUI(episode)
            checkIfEpisodeIsViewed(episode.id.toString()) // Comprobar estado real en la nube
        }

        return view
    }

    private fun setupUI(episode: Episode) {
        tvName.text = episode.name
        tvCode.text = episode.episode
        tvDate.text = episode.airDate

        // Listener del Switch: Se activa cuando el usuario toca
        switchViewed.setOnCheckedChangeListener { _, isChecked ->
            saveViewedState(episode, isChecked)
        }
    }

    // 1. COMPROBAR EN LA NUBE SI YA ESTABA VISTO
    private fun checkIfEpisodeIsViewed(episodeId: String) {
        val userId = auth.currentUser?.uid ?: return

        // Ruta: users -> {uid} -> viewed_episodes -> {episodeId}
        db.collection("users").document(userId)
            .collection("viewed_episodes").document(episodeId)
            .get()
            .addOnSuccessListener { document ->
                // Si el documento existe, es que está visto.
                // Ponemos el switch a true/false SIN disparar el listener de nuevo (truco visual)
                switchViewed.setOnCheckedChangeListener(null)
                switchViewed.isChecked = document.exists()
                switchViewed.setOnCheckedChangeListener { _, isChecked ->
                    // Volvemos a activar el listener
                    // (Necesitamos el objeto episode aquí, pero como esta función es llamada
                    // desde onCreateView donde tenemos 'episode', lo ideal es refactorizar un poco.
                    // Para simplificar, usaremos el argumento global o pasamos el episodio a esta función)

                    // Nota: Para corregir el ámbito, mira la función saveViewedState abajo.
                    // Simplemente reactivamos el listener genérico aquí es complicado sin el objeto.
                    // MEJOR ESTRATEGIA: No quitar el listener, pero controlar el bucle.
                }

                // RE-VINCULAMOS EL LISTENER CORRECTAMENTE CON EL EPISODIO
                val episode = arguments?.getParcelable<Episode>("episode_data")
                if (episode != null) {
                    switchViewed.setOnCheckedChangeListener { _, isChecked ->
                        saveViewedState(episode, isChecked)
                    }
                }
            }
    }

    // 2. GUARDAR O BORRAR EN FIRESTORE
    private fun saveViewedState(episode: Episode, isViewed: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(userId)
        val episodeDocRef = userDocRef.collection("viewed_episodes").document(episode.id.toString())

        if (isViewed) {
            // A) Si marcamos como visto -> CREAMOS EL DOCUMENTO
            // Guardamos datos útiles para estadísticas (Apartado F)
            val data = hashMapOf(
                "id" to episode.id,
                "name" to episode.name,
                "episode" to episode.episode,
                "air_date" to episode.airDate,
                "viewed_at" to System.currentTimeMillis()
            )

            episodeDocRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, "Marcado como visto ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                    switchViewed.isChecked = false // Revertir switch
                }
        } else {
            // B) Si desmarcamos -> BORRAMOS EL DOCUMENTO
            episodeDocRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Marcado como NO visto ❌", Toast.LENGTH_SHORT).show()
                }
        }
    }
}