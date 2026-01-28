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

/**
 * Pantalla principal que muestra el listado de episodios.
 * * Flujo de datos:
 * 1. Descarga episodios de la API (Retrofit).
 * 2. Descarga los IDs "vistos" de Firebase.
 * 3. Fusiona ambas listas (merge) para marcar visualmente los vistos.
 * * Funcionalidad extra: Selección múltiple y guardado por lotes (Batch Write).
 */
class EpisodeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: EpisodeAdapter
    private lateinit var rgFilter: RadioGroup
    private lateinit var fabAction: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Lista maestra para filtrar sin volver a pedir datos
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

        // Inicializamos el adaptador pasando las funciones lambda para los eventos
        adapter = EpisodeAdapter(
            episodes = emptyList(),
            onClick = { episode ->
                // Navegación al detalle pasando el objeto Parcelable
                val bundle = Bundle().apply {
                    putParcelable("episode_data", episode)
                }
                findNavController().navigate(R.id.action_list_to_detail, bundle)
            },
            onSelectionChanged = { isSelectionMode ->
                // Mostramos el FAB solo si hay elementos seleccionados
                fabAction.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
            }
        )
        recyclerView.adapter = adapter

        // Listener para filtrado local (Todos / Vistos)
        rgFilter.setOnCheckedChangeListener { _, checkedId ->
            filterList(checkedId)
        }

        // Acción del FAB: Guardar selección en la nube
        fabAction.setOnClickListener {
            saveSelectedEpisodes()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recargamos datos al volver (por si marcamos como visto en el detalle)
        loadEpisodes()
    }

    private fun loadEpisodes() {
        progressBar.visibility = View.VISIBLE
        // 1. Petición asíncrona a la API
        RetrofitClient.apiService.getEpisodes(1).enqueue(object : Callback<EpisodeResponse> {
            override fun onResponse(call: Call<EpisodeResponse>, response: Response<EpisodeResponse>) {
                if (response.isSuccessful) {
                    val apiList = response.body()?.results ?: emptyList()
                    // 2. Si hay éxito, cruzamos datos con Firebase
                    mergeWithFirestore(apiList)
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<EpisodeResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                // Aquí podríamos mostrar un layout de error de conexión
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

        // Consultamos la colección de vistos
        db.collection("users").document(userId).collection("viewed_episodes")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                // Creamos un Set de IDs para búsqueda rápida O(1)
                val viewedIds = documents.map { it.id }.toSet()

                // Recorremos la lista de la API y marcamos los que coincidan
                apiEpisodes.forEach { episode ->
                    if (viewedIds.contains(episode.id.toString())) {
                        episode.isViewed = true
                    }
                }
                fullList = apiEpisodes
                // Aplicamos el filtro actual (por defecto "Todos")
                filterList(rgFilter.checkedRadioButtonId)
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                // Si falla Firebase, mostramos la lista tal cual viene de la API
                fullList = apiEpisodes
                adapter.updateList(fullList)
            }
    }

    private fun filterList(checkedId: Int) {
        if (fullList.isEmpty()) return

        // Filtrado en memoria (no requiere red)
        val listToShow = if (checkedId == R.id.rbViewed) {
            fullList.filter { it.isViewed }
        } else {
            fullList
        }
        adapter.updateList(listToShow)
    }

    /**
     * Guarda múltiples episodios a la vez usando Firestore Batch.
     * Esto es mucho más eficiente que hacer un loop de peticiones individuales.
     */
    private fun saveSelectedEpisodes() {
        val userId = auth.currentUser?.uid ?: return
        val selectedEpisodes = adapter.getSelectedEpisodes()

        if (selectedEpisodes.isEmpty()) return

        progressBar.visibility = View.VISIBLE

        // Instancia de Batch (Lote de escritura)
        val batch = db.batch()
        val userRef = db.collection("users").document(userId)
        val episodesRef = userRef.collection("viewed_episodes")

        var markedCount = 0

        selectedEpisodes.forEach { episode ->
            if (!episode.isViewed) {
                val docRef = episodesRef.document(episode.id.toString())
                val data = hashMapOf(
                    "id" to episode.id,
                    "name" to episode.name,
                    "episode" to episode.episode,
                    "air_date" to episode.airDate,
                    "viewed_at" to System.currentTimeMillis()
                )
                // Añadimos la operación al lote
                batch.set(docRef, data, SetOptions.merge())
                markedCount++
            }
        }

        if (markedCount == 0) {
            progressBar.visibility = View.GONE
            adapter.clearSelection()
            Toast.makeText(context, getString(R.string.detail_not_viewed_toast), Toast.LENGTH_SHORT).show()
            return
        }

        // Ejecutamos todas las escrituras de golpe (Commit)
        batch.commit().addOnSuccessListener {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "$markedCount episodios guardados", Toast.LENGTH_SHORT).show()

            // Actualizamos visualmente la lista local para reflejar el cambio inmediato
            selectedEpisodes.forEach { it.isViewed = true }
            adapter.clearSelection()
            filterList(rgFilter.checkedRadioButtonId)

        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
        }
    }
}