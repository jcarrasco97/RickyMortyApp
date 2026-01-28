package com.example.rickymortyapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
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
import java.util.Locale

class SettingsFragment : Fragment() {

    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var rgLanguage: RadioGroup
    private lateinit var rbSpanish: RadioButton
    private lateinit var rbEnglish: RadioButton
    private lateinit var btnLogout: Button

    // Archivo de preferencias
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Inicializar preferencias (Nombre del archivo: "app_settings")
        prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        rgLanguage = view.findViewById(R.id.rgLanguage)
        rbSpanish = view.findViewById(R.id.rbSpanish)
        rbEnglish = view.findViewById(R.id.rbEnglish)
        btnLogout = view.findViewById(R.id.btnLogoutSettings)

        loadSavedPreferences()
        setupListeners()

        return view
    }

    private fun loadSavedPreferences() {
        // 1. Cargar Tema
        val isDark = prefs.getBoolean("dark_mode", true) // Por defecto true (Oscuro)
        switchDarkMode.isChecked = isDark

        // 2. Cargar Idioma
        val language = prefs.getString("language", "es")
        if (language == "en") {
            rbEnglish.isChecked = true
        } else {
            rbSpanish.isChecked = true
        }
    }

    private fun setupListeners() {
        // CAMBIO DE TEMA
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Guardar en prefs
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            // Aplicar tema
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // CAMBIO DE IDIOMA
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val langCode = if (checkedId == R.id.rbEnglish) "en" else "es"

            // Guardar solo si es diferente al actual para evitar bucles
            val currentLang = prefs.getString("language", "es")
            if (currentLang != langCode) {
                prefs.edit().putString("language", langCode).apply()
                setLocale(langCode)
            }
        }

        // CERRAR SESIÓN
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        // Actualizar configuración
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)

        // Reiniciar la actividad para aplicar cambios de texto
        requireActivity().recreate()
    }
}