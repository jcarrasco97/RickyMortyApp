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

        rvCharacters.layoutManager = GridLayoutManager(context, 3)
        charAdapter = CharacterAdapter(emptyList())
        rvCharacters.adapter = charAdapter

        val episode = arguments?.getParcelable<Episode>("episode_data")

        if (episode != null) {
            setupUI(episode)
            checkIfEpisodeIsViewed(episode.id.toString())
        }

        return view
    }

    private fun setupUI(episode: Episode) {
        tvName.text = episode.name
        tvCode.text = episode.episode
        tvDate.text = episode.airDate

        switchViewed.setOnCheckedChangeListener { _, isChecked ->
            saveViewedState(episode, isChecked)
        }

        loadCharacters(episode.characters)
    }

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
                // Silencio
            }
        })
    }

    private fun checkIfEpisodeIsViewed(episodeId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("viewed_episodes").document(episodeId)
            .get()
            .addOnSuccessListener { document ->
                switchViewed.setOnCheckedChangeListener(null)
                switchViewed.isChecked = document.exists()

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
            val data = hashMapOf(
                "id" to episode.id,
                "name" to episode.name,
                "episode" to episode.episode,
                "air_date" to episode.airDate,
                "viewed_at" to System.currentTimeMillis()
            )
            episodeDocRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    // CORREGIDO: Mensaje traducible
                    Toast.makeText(context, getString(R.string.detail_viewed_toast), Toast.LENGTH_SHORT).show()
                }
        } else {
            episodeDocRef.delete()
                .addOnSuccessListener {
                    // CORREGIDO: Mensaje traducible
                    Toast.makeText(context, getString(R.string.detail_not_viewed_toast), Toast.LENGTH_SHORT).show()
                }
        }
    }
}