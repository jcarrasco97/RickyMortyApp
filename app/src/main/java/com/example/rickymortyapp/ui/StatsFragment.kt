package com.example.rickymortyapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rickymortyapp.R
import com.example.rickymortyapp.models.EpisodeResponse
import com.example.rickymortyapp.network.RetrofitClient
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatsFragment : Fragment() {

    private lateinit var tvSummary: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var pieChart: PieChart

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        tvSummary = view.findViewById(R.id.tvStatsSummary)
        tvPercentage = view.findViewById(R.id.tvPercentage)
        pieChart = view.findViewById(R.id.pieChart)

        setupChartDesign()
        loadData()

        return view
    }

    private fun setupChartDesign() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setEntryLabelColor(Color.BLACK)
    }

    private fun loadData() {
        RetrofitClient.apiService.getEpisodes(1).enqueue(object : Callback<EpisodeResponse> {
            override fun onResponse(call: Call<EpisodeResponse>, response: Response<EpisodeResponse>) {
                if (response.isSuccessful) {
                    val totalEpisodes = response.body()?.info?.count ?: 0
                    getViewedCount(totalEpisodes)
                }
            }

            override fun onFailure(call: Call<EpisodeResponse>, t: Throwable) {
                // CORREGIDO: Usamos R.string.stats_error
                if (isAdded) { // Comprobamos isAdded para evitar crash si el fragmento se cerró
                    tvSummary.text = getString(R.string.stats_error)
                }
            }
        })
    }

    private fun getViewedCount(total: Int) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("viewed_episodes")
            .get()
            .addOnSuccessListener { documents ->
                val viewed = documents.size()
                updateUI(viewed, total)
            }
            .addOnFailureListener {
                // CORREGIDO: Usamos R.string.stats_error
                if (isAdded) {
                    tvSummary.text = getString(R.string.stats_error)
                }
            }
    }

    private fun updateUI(viewed: Int, total: Int) {
        if (!isAdded) return // Seguridad

        val notViewed = total - viewed
        val percentage = if (total > 0) (viewed * 100) / total else 0

        // CORREGIDO: Usamos getString con placeholders %1$d y %2$d
        tvSummary.text = getString(R.string.stats_summary, viewed, total)
        tvPercentage.text = "$percentage%"

        val entries = ArrayList<PieEntry>()
        // CORREGIDO: Usamos textos traducibles para el gráfico
        entries.add(PieEntry(viewed.toFloat(), getString(R.string.filter_viewed))) // Vistos
        entries.add(PieEntry(notViewed.toFloat(), getString(R.string.filter_all))) // Restantes (Usamos 'Todos' o 'Pendientes' si creaste string)

        val dataSet = PieDataSet(entries, getString(R.string.stats_title))

        dataSet.colors = listOf(
            Color.parseColor("#00FF9C"),
            Color.parseColor("#444444")
        )

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate()
        pieChart.animateY(1400)
    }
}