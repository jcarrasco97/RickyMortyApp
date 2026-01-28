package com.example.rickymortyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Actividad de Registro.
 * Permite crear una nueva cuenta de usuario en Firebase Authentication.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvBack: TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnDoRegister)
        tvBack = findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Llamada asíncrona a Firebase para crear usuario
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            // Cerramos la actividad para volver al Login automáticamente
                            finish()
                        } else {
                            // Manejo de errores (ej: email mal formado, contraseña corta)
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show()
            }
        }

        tvBack.setOnClickListener {
            finish() // Volver atrás sin registrar
        }
    }
}