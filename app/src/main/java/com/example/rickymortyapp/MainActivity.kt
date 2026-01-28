package com.example.rickymortyapp

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

/**
 * Actividad Principal (Contenedor).
 * Implementa el patrón Navigation Drawer (Menú lateral) y gestiona el NavHostFragment
 * donde se cargan las distintas pantallas (Lista, Stats, Settings).
 *
 * También es responsable de aplicar la configuración global de Idioma y Tema
 * antes de que se inflen las vistas.
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private val auth = FirebaseAuth.getInstance()

    /**
     * Método crítico para la internacionalización.
     * Se ejecuta ANTES de onCreate. Intercepta el contexto base de la aplicación,
     * lee la preferencia de idioma del usuario y fuerza esa configuración (Locale)
     * en el contexto de la actividad.
     * Esto asegura que los recursos (strings.xml) se carguen en el idioma correcto.
     */
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "es") ?: "es"

        val locale = Locale(language)
        val config = Configuration(newBase.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)

        // Creamos un contexto nuevo con la configuración modificada y lo pasamos al super
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lectura y aplicación del Tema (Claro / Oscuro)
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // Vinculación de vistas del menú lateral
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        // Configuración del botón "Hamburguesa" para abrir/cerrar el menú
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configuración del Jetpack Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        NavigationUI.setupWithNavController(navigationView, navController)
        navigationView.setNavigationItemSelectedListener(this)
    }

    /**
     * Gestión de los clicks en el menú lateral.
     * Permite navegación manual o acciones específicas como Logout y Diálogos.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        when (item.itemId) {
            R.id.nav_episodes -> {
                // Limpiamos la pila de navegación para evitar acumulación de fragmentos
                navController.popBackStack(R.id.nav_episodes, false)
                navController.navigate(R.id.nav_episodes)
            }
            R.id.nav_about -> {
                showAboutDialog()
            }
            R.id.nav_logout -> {
                auth.signOut()
                // Al cerrar sesión, redirigimos al Login y finalizamos esta Activity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> {
                // Navegación estándar de Android Jetpack
                NavigationUI.onNavDestinationSelected(item, navController)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.menu_about))
            .setMessage("Rick and Morty App\n\nVersión: 1.0.0\n\nJuan Antonio Carrasco Sánchez\n\nI.E.S. Aguadulce - Curso 2025/26")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}