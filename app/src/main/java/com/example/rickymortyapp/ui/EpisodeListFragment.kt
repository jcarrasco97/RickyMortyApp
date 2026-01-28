package com.example.rickymortyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode
import com.example.rickymortyapp.models.EpisodeResponse
import com.example.rickymortyapp.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EpisodeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: EpisodeAdapter
    private lateinit var rgFilter: RadioGroup
    private lateinit var fabAction: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var fullList: List<Episode> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_episode_list, container, false)

        recyclerView = view.findViewById(R.id.rvEpisodes)
        progressBar = view.findViewById(R.id.progressBar)
        rgFilter = view.findViewById(R.id.rgFilter)
        fabAction = view.findViewById(R.id.fabAction)

        // CONFIGURAR ADAPTER CON LÓGICA DE SELECCIÓN
        adapter = EpisodeAdapter(
            episodes = emptyList(),
            onClick = { episode ->
                // Click normal: Ir al detalle
                val bundle = Bundle().apply {
                    putParcelable("episode_data", episode)
                }
                findNavController().navigate(R.id.action_list_to_detail, bundle)
            },
            onSelectionChanged = { isSelectionMode ->
                // Mostrar u ocultar el botón flotante
                if (isSelectionMode) {
                    fabAction.visibility = View.VISIBLE
                } else {
                    fabAction.visibility = View.GONE
                }
            }
        )
        recyclerView.adapter = adapter

        // Listener del Filtro
        rgFilter.setOnCheckedChangeListener { _, checkedId ->
            filterList(checkedId)
        }

        // Listener del Botón Flotante (Guardar selección)
        fabAction.setOnClickListener {
            saveSelectedEpisodes()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadEpisodes()
    }

    private fun loadEpisodes() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.apiService.getEpisodes(1).enqueue(object : Callback<EpisodeResponse> {
            override fun onResponse(call: Call<EpisodeResponse>, response: Response<EpisodeResponse>) {
                if (response.isSuccessful) {
                    val apiList = response.body()?.results ?: emptyList()
                    mergeWithFirestore(apiList)
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<EpisodeResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun mergeWithFirestore(apiEpisodes: List<Episode>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            progressBar.visibility = View.GONE
            fullList = apiEpisodes
            filterList(rgFilter.checkedRadioButtonId)
            return
        }

        db.collection("users").document(userId).collection("viewed_episodes")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                val viewedIds = documents.map { it.id }.toSet()

                apiEpisodes.forEach { episode ->
                    if (viewedIds.contains(episode.id.toString())) {
                        episode.isViewed = true
                    }
                }
                fullList = apiEpisodes
                filterList(rgFilter.checkedRadioButtonId)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                fullList = apiEpisodes
                adapter.updateList(fullList)
            }
    }

    private fun filterList(checkedId: Int) {
        if (fullList.isEmpty()) return
        val listToShow = if (checkedId == R.id.rbViewed) {
            fullList.filter { it.isViewed }
        } else {
            fullList
        }
        adapter.updateList(listToShow)
    }

    // --- LÓGICA DE GUARDADO MÚLTIPLE (BATCH) ---
    private fun saveSelectedEpisodes() {
        val userId = auth.currentUser?.uid ?: return
        val selectedEpisodes = adapter.getSelectedEpisodes()

        if (selectedEpisodes.isEmpty()) return

        progressBar.visibility = View.VISIBLE

        // Usamos un WriteBatch para guardar muchos de golpe (Eficiencia Firestore)
        val batch = db.batch()
        val userRef = db.collection("users").document(userId)
        val episodesRef = userRef.collection("viewed_episodes")

        var markedCount = 0

        selectedEpisodes.forEach { episode ->
            // Si ya estaba visto, no hacemos nada (o podríamos borrarlo si quisiéramos lógica inversa)
            // Aquí asumimos que "Seleccionar" -> "Marcar como Visto"
            if (!episode.isViewed) {
                val docRef = episodesRef.document(episode.id.toString())
                val data = hashMapOf(
                    "id" to episode.id,
                    "name" to episode.name,
                    "episode" to episode.episode,
                    "air_date" to episode.airDate,
                    "viewed_at" to System.currentTimeMillis()
                )
                batch.set(docRef, data, SetOptions.merge())
                markedCount++
            }
        }

        // Si no hay nada nuevo que marcar, solo limpiamos
        if (markedCount == 0) {
            progressBar.visibility = View.GONE
            adapter.clearSelection()
            Toast.makeText(context, "No hay cambios pendientes", Toast.LENGTH_SHORT).show()
            return
        }

        // Ejecutar el lote
        batch.commit().addOnSuccessListener {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "$markedCount episodios marcados como vistos", Toast.LENGTH_SHORT).show()

            // Actualizar la lista local visualmente
            selectedEpisodes.forEach { it.isViewed = true }
            adapter.clearSelection()

            // Refrescar el filtro por si estamos en la pestaña "Vistos"
            filterList(rgFilter.checkedRadioButtonId)

        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }
}