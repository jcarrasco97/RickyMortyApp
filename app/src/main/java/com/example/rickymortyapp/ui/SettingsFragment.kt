package com.example.rickymortyapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.rickymortyapp.LoginActivity
import com.example.rickymortyapp.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var rgLanguage: RadioGroup
    private lateinit var rbSpanish: RadioButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var btnLogout: Button

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        rgLanguage = view.findViewById(R.id.rgLanguage)
        rbSpanish = view.findViewById(R.id.rbSpanish)
        rbEnglish = view.findViewById(R.id.rbEnglish)
        btnLogout = view.findViewById(R.id.btnLogoutSettings)

        // 1. Cargar estado visual
        loadSavedPreferences()

        // 2. Activar listeners
        setupListeners()

        return view
    }

    private fun loadSavedPreferences() {
        // Cargar Tema
        val isDark = prefs.getBoolean("dark_mode", true)
        switchDarkMode.setOnCheckedChangeListener(null) // Pausar listener
        switchDarkMode.isChecked = isDark

        // Cargar Idioma
        val language = prefs.getString("language", "es")
        rgLanguage.setOnCheckedChangeListener(null) // Pausar listener
        if (language == "en") {
            rbEnglish.isChecked = true
        } else {
            rbSpanish.isChecked = true
        }
    }

    private fun setupListeners() {
        // --- MODO OSCURO ---
        switchDarkMode.setOnClickListener {
            val isChecked = switchDarkMode.isChecked
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- IDIOMA ---
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            // Determinar qué idioma ha seleccionado el usuario
            val selectedLang = if (checkedId == R.id.rbEnglish) "en" else "es"

            // Leer el idioma que teníamos guardado
            val currentLang = prefs.getString("language", "es")

            // Solo actuamos si son diferentes (para evitar bucles)
            if (selectedLang != currentLang) {
                // 1. Guardamos de forma SÍNCRONA (commit) para asegurar que se escribe YA
                prefs.edit().putString("language", selectedLang).commit()

                // 2. Reiniciamos la actividad.
                // Como hemos modificado MainActivity.attachBaseContext,
                // al renacer leerá el nuevo idioma automáticamente.
                requireActivity().recreate()
            }
        }

        // --- LOGOUT ---
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}