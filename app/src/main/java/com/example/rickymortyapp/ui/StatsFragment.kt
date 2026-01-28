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
        // Configuración visual básica del gráfico (sin datos aún)
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.legend.isEnabled = false
        pieChart.setHoleColor(Color.TRANSPARENT) // Agujero transparente
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setEntryLabelColor(Color.BLACK)
    }

    private fun loadData() {
        // 1. OBTENER TOTAL DE EPISODIOS (API)
        RetrofitClient.apiService.getEpisodes(1).enqueue(object : Callback<EpisodeResponse> {
            override fun onResponse(call: Call<EpisodeResponse>, response: Response<EpisodeResponse>) {
                if (response.isSuccessful) {
                    val totalEpisodes = response.body()?.info?.count ?: 0
                    // Una vez tenemos el total, pedimos los vistos
                    getViewedCount(totalEpisodes)
                }
            }

            override fun onFailure(call: Call<EpisodeResponse>, t: Throwable) {
                tvSummary.text = "Error de conexión"
            }
        })
    }

    private fun getViewedCount(total: Int) {
        val userId = auth.currentUser?.uid ?: return

        // 2. OBTENER VISTOS (FIREBASE)
        db.collection("users").document(userId).collection("viewed_episodes")
            .get()
            .addOnSuccessListener { documents ->
                val viewed = documents.size()
                updateUI(viewed, total)
            }
            .addOnFailureListener {
                tvSummary.text = "Error al cargar estadísticas"
            }
    }

    private fun updateUI(viewed: Int, total: Int) {
        // Calcular porcentaje
        val notViewed = total - viewed
        val percentage = if (total > 0) (viewed * 100) / total else 0

        // Actualizar Textos
        tvSummary.text = "Has visto $viewed de $total episodios"
        tvPercentage.text = "$percentage%"

        // Crear datos para el gráfico
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(viewed.toFloat(), "Vistos"))
        entries.add(PieEntry(notViewed.toFloat(), "Pendientes"))

        val dataSet = PieDataSet(entries, "Resultados")

        // Colores: Verde Neon (#00FF9C) y Gris Oscuro (#444444)
        dataSet.colors = listOf(
            Color.parseColor("#00FF9C"), // Vistos
            Color.parseColor("#444444")  // No vistos
        )

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.invalidate() // Refrescar gráfico
        pieChart.animateY(1400) // Animación chula
    }
}