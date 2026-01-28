package com.example.rickymortyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Usamos un Handler para esperar 3 segundos (3000ms)
        Handler(Looper.getMainLooper()).postDelayed({

            // Decidimos a dónde ir:
            // Si quieres comprobar si ya hay usuario logueado, podrías hacerlo aquí.
            // Por simplicidad, mandamos siempre al Login (o al Main si recuerdas sesión).
            // En tu caso, mandamos al LoginActivity que ya gestiona su lógica.

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Matamos la SplashActivity para que no se pueda volver atrás
            finish()

        }, 3000) // 3 segundos de espera
    }
}