package com.example.rickymortyapp // TU PAQUETE

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private val auth = FirebaseAuth.getInstance()

    // 1. AQUÍ ES DONDE SE APLICA EL IDIOMA DE FORMA MODERNA
    // Se ejecuta antes de onCreate para inyectar la configuración correcta
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"

        // Creamos la configuración con el idioma guardado
        val locale = Locale(language)
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)

        // Creamos un nuevo contexto con esa configuración
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. APLICAR TEMA (Esto sí puede ir en onCreate)
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // 3. Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        // 4. Configurar Toolbar
        setSupportActionBar(toolbar)

        // 5. Configurar el Toggle (Hamburguesa)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 6. Configurar navegación
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Vincula menú con grafo automáticamente
        NavigationUI.setupWithNavController(navigationView, navController)

        // 7. Listener manual para controlar Logout y Acerca De
        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            // --- AQUÍ VA EL BOTÓN ACERCA DE ---
            R.id.nav_about -> {
                showAboutDialog()
            }

            // --- BOTÓN CERRAR SESIÓN ---
            R.id.nav_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            // --- RESTO DE OPCIONES (Navegación normal) ---
            else -> {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                // Si NavigationUI no lo maneja, cerramos el drawer nosotros
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }

        // Cerrar menú siempre después de pulsar
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Función para mostrar el diálogo de Acerca De
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acerca de")
            .setMessage("Rick y Morty App\n\nDesarrollado por: Juan Carrasco\nVersión: 1.0.0\n\nCurso 2025/26")
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}