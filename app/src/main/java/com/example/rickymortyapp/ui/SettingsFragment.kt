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

/**
 * Fragmento de configuración.
 * Permite cambiar el tema (Claro/Oscuro), el idioma (ES/EN) y cerrar sesión.
 * Utiliza SharedPreferences para persistir las elecciones del usuario.
 */
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

        // Cargamos las preferencias guardadas antes de activar los listeners
        loadSavedPreferences()
        setupListeners()

        return view
    }

    private fun loadSavedPreferences() {
        val isDark = prefs.getBoolean("dark_mode", true)
        // Desactivamos listener para evitar disparos accidentales al setear el valor inicial
        switchDarkMode.setOnCheckedChangeListener(null)
        switchDarkMode.isChecked = isDark

        val language = prefs.getString("language", "es")
        rgLanguage.setOnCheckedChangeListener(null)
        if (language == "en") {
            rbEnglish.isChecked = true
        } else {
            rbSpanish.isChecked = true
        }
    }

    private fun setupListeners() {
        // Listener Modo Oscuro
        switchDarkMode.setOnClickListener {
            val isChecked = switchDarkMode.isChecked
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Listener Idioma
        rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            val selectedLang = if (checkedId == R.id.rbEnglish) "en" else "es"
            val currentLang = prefs.getString("language", "es")

            // Solo actuamos si el idioma ha cambiado realmente
            if (selectedLang != currentLang) {
                // Guardamos síncronamente (commit) para asegurar que el dato está escrito antes de reiniciar
                prefs.edit().putString("language", selectedLang).commit()

                // Reiniciamos la Activity para que el nuevo ContextWrapper (attachBaseContext)
                // cargue los recursos en el nuevo idioma.
                requireActivity().recreate()
            }
        }

        // Logout
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Limpiamos la pila de actividades (flags) para impedir volver atrás
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}