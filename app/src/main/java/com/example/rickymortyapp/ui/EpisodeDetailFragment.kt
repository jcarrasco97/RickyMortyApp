package com.example.rickymortyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode
import com.example.rickymortyapp.network.RetrofitClient
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragmento de detalle. Muestra información extendida del episodio.
 * Realiza una segunda llamada a la API para obtener los personajes específicos de este capítulo.
 */
class EpisodeDetailFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvCode: TextView
    private lateinit var tvDate: TextView
    private lateinit var switchViewed: SwitchMaterial

    private lateinit var rvCharacters: RecyclerView
    private lateinit var charAdapter: CharacterAdapter

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
        rvCharacters = view.findViewById(R.id.rvCharacters)

        // Configuración del Grid para los personajes (3 columnas)
        rvCharacters.layoutManager = GridLayoutManager(context, 3)
        charAdapter = CharacterAdapter(emptyList())
        rvCharacters.adapter = charAdapter

        // Recibimos el objeto Episode completo pasado desde el fragmento anterior
        @Suppress("DEPRECATION") // getParcelable(key) está deprecado en API 33+, pero es seguro aquí.
        val episode = arguments?.getParcelable<Episode>("episode_data")

        if (episode != null) {
            setupUI(episode)
            // Comprobamos en tiempo real si este episodio está marcado como visto en Firestore
            checkIfEpisodeIsViewed(episode.id.toString())
        }

        return view
    }

    private fun setupUI(episode: Episode) {
        tvName.text = episode.name
        tvCode.text = episode.episode
        tvDate.text = episode.airDate

        // Listener para marcar/desmarcar como visto
        switchViewed.setOnCheckedChangeListener { _, isChecked ->
            saveViewedState(episode, isChecked)
        }

        loadCharacters(episode.characters)
    }

    /**
     * Extrae los IDs de las URLs de los personajes y hace una petición a la API.
     * Ejemplo URLs: [".../character/1", ".../character/2"] -> Petición: "character/1,2"
     */
    private fun loadCharacters(urls: List<String>) {
        if (urls.isEmpty()) return

        val ids = urls.map { url ->
            url.substringAfterLast("/")
        }.joinToString(",")

        RetrofitClient.apiService.getMultipleCharacters(ids).enqueue(object : Callback<List<com.example.rickymortyapp.models.Character>> {
            override fun onResponse(
                call: Call<List<com.example.rickymortyapp.models.Character>>,
                response: Response<List<com.example.rickymortyapp.models.Character>>
            ) {
                if (response.isSuccessful) {
                    val charList = response.body() ?: emptyList()
                    charAdapter.updateList(charList)
                }
            }

            override fun onFailure(call: Call<List<com.example.rickymortyapp.models.Character>>, t: Throwable) {
                // Fallo silencioso en la UI, podríamos mostrar un Toast si fuera crítico.
            }
        })
    }

    private fun checkIfEpisodeIsViewed(episodeId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("viewed_episodes").document(episodeId)
            .get()
            .addOnSuccessListener { document ->
                // IMPORTANTE: Quitamos el listener temporalmente para evitar bucles infinitos
                // al establecer el estado inicial del switch.
                switchViewed.setOnCheckedChangeListener(null)
                switchViewed.isChecked = document.exists()

                // Reactivamos el listener con los datos actualizados
                @Suppress("DEPRECATION")
                val episode = arguments?.getParcelable<Episode>("episode_data")
                if (episode != null) {
                    switchViewed.setOnCheckedChangeListener { _, isChecked ->
                        saveViewedState(episode, isChecked)
                    }
                }
            }
    }

    private fun saveViewedState(episode: Episode, isViewed: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(userId)
        val episodeDocRef = userDocRef.collection("viewed_episodes").document(episode.id.toString())

        if (isViewed) {
            // Guardamos metadatos básicos para poder consultarlos luego sin llamar a la API
            val data = hashMapOf(
                "id" to episode.id,
                "name" to episode.name,
                "episode" to episode.episode,
                "air_date" to episode.airDate,
                "viewed_at" to System.currentTimeMillis()
            )
            // SetOptions.merge() evita sobrescribir campos si existieran otros
            episodeDocRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(context, getString(R.string.detail_viewed_toast), Toast.LENGTH_SHORT).show()
                }
        } else {
            // Si desmarcamos, borramos el documento de la colección
            episodeDocRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(context, getString(R.string.detail_not_viewed_toast), Toast.LENGTH_SHORT).show()
                }
        }
    }
}