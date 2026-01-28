package com.example.rickymortyapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Importante para navegar
import androidx.recyclerview.widget.RecyclerView
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.Episode
import com.example.rickymortyapp.models.EpisodeResponse
import com.example.rickymortyapp.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EpisodeListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: EpisodeAdapter

    // Instancias de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_episode_list, container, false)

        recyclerView = view.findViewById(R.id.rvEpisodes)
        progressBar = view.findViewById(R.id.progressBar)

        // Configurar el click para ir al detalle
        adapter = EpisodeAdapter(emptyList()) { episode ->
            val bundle = Bundle().apply {
                putParcelable("episode_data", episode)
            }
            findNavController().navigate(R.id.action_list_to_detail, bundle)
        }
        recyclerView.adapter = adapter

        return view
    }

    // Usamos onResume para que la lista se refresque al volver del Detalle
    override fun onResume() {
        super.onResume()
        loadEpisodes()
    }

    private fun loadEpisodes() {
        progressBar.visibility = View.VISIBLE

        // 1. PEDIR DATOS A LA API (Retrofit)
        RetrofitClient.apiService.getEpisodes(1).enqueue(object : Callback<EpisodeResponse> {
            override fun onResponse(call: Call<EpisodeResponse>, response: Response<EpisodeResponse>) {
                if (response.isSuccessful) {
                    val apiList = response.body()?.results ?: emptyList()

                    // 2. EN LUGAR DE MOSTRARLOS DIRECTAMENTE, CONSULTAMOS FIRESTORE
                    mergeWithFirestore(apiList)

                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error API: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<EpisodeResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // LA MAGIA: CRUCE DE DATOS API + FIREBASE
    private fun mergeWithFirestore(apiEpisodes: List<Episode>) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // Si no hay usuario, mostramos la lista tal cual (sin vistos)
            progressBar.visibility = View.GONE
            adapter.updateList(apiEpisodes)
            return
        }

        // Descargamos TODOS los episodios vistos por el usuario
        db.collection("users").document(userId).collection("viewed_episodes")
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE

                // Creamos una lista rápida con los IDs que están en Firebase
                // Ejemplo: viewedIds = ["1", "5", "10"]
                val viewedIds = documents.map { it.id }.toSet()

                // Recorremos la lista de la API y marcamos los que coinciden
                apiEpisodes.forEach { episode ->
                    if (viewedIds.contains(episode.id.toString())) {
                        episode.isViewed = true
                    }
                }

                // 3. ACTUALIZAMOS EL ADAPTADOR CON LA LISTA MEZCLADA
                adapter.updateList(apiEpisodes)
            }
            .addOnFailureListener {
                // Si falla Firebase, mostramos la lista al menos
                progressBar.visibility = View.GONE
                adapter.updateList(apiEpisodes)
            }
    }
}