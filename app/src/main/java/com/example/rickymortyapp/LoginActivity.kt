package com.example.rickymortyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Declaramos las variables de la vista
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    // Instancia de Firebase Auth
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Vinculamos las variables con los IDs del XML
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Configurar botón LOGIN
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Login correcto -> Ir a MainActivity
                            goToHome()
                        } else {
                            showAlert("Error al iniciar sesión: ${task.exception?.message}")
                        }
                    }
            } else {
                showAlert("Rellena todos los campos")
            }
        }

        // Configurar botón REGISTRO
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showAlert("¡Usuario registrado! Ahora inicia sesión.")
                            // Opcional: Podrías ir directo a Home, pero mejor que se loguee
                        } else {
                            showAlert("Error al registrar: ${task.exception?.message}")
                        }
                    }
            } else {
                showAlert("Rellena todos los campos")
            }
        }
    }

    private fun showAlert(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Cierra el Login para que no se pueda volver atrás
    }
}