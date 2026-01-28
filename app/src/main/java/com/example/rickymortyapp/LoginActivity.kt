package com.example.rickymortyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad de Inicio de Sesión.
 * Es el punto de entrada de la aplicación para usuarios no autenticados.
 * Gestiona la comunicación con Firebase Authentication para validar credenciales.
 */
class LoginActivity : AppCompatActivity() {

    // Componentes de la interfaz de usuario
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Instancia de Firebase Auth.
    // Es el objeto singleton que gestiona la sesión del usuario en la nube.
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Vinculación de vistas (Binding manual mediante findViewById)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Lógica del botón LOGIN
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Llamada asíncrona a Firebase para validar el usuario.
                // No bloquea el hilo principal (UI Thread).
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Si las credenciales son correctas, navegamos a la pantalla principal
                            goToHome()
                        } else {
                            // Si falla (contraseña incorrecta, usuario no existe, sin internet...),
                            // mostramos el error que nos devuelve Firebase.
                            showAlert("Error al iniciar sesión: ${task.exception?.message}")
                        }
                    }
            } else {
                showAlert("Rellena todos los campos")
            }
        }

        // Lógica del botón REGISTRO
        // Navegación explícita hacia la actividad de creación de cuenta.
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Muestra un mensaje emergente (Toast) de corta duración.
     * Útil para feedback rápido al usuario.
     */
    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Navegación a la pantalla principal (MainActivity).
     * IMPORTANTE: Usamos finish() para "matar" la actividad de Login.
     * Esto evita que, si el usuario pulsa el botón "Atrás" desde la pantalla principal,
     * vuelva a aparecer la pantalla de Login (ya que la sesión está iniciada).
     */
    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}