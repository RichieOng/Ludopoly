package com.ludopoly.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.ludopoly.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        handlePrefilledEmail(intent)
        setupListeners()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePrefilledEmail(intent)
    }

    private fun handlePrefilledEmail(intent: Intent?) {
        val registeredEmail = intent?.getStringExtra("EXTRA_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            binding.etEmail.setText(registeredEmail)
            binding.etPassword.requestFocus()
        }
    }

    private fun setupListeners() {
        // Tombol Masuk
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        // Teks Lupa Sandi
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Teks Daftar Akun
        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi input
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email wajib diisi"
            binding.etEmail.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Kata sandi wajib diisi"
            binding.etPassword.requestFocus()
            return
        } else if (password.length < 6) {
            binding.tilPassword.error = "Kata sandi minimal 6 karakter"
            binding.etPassword.requestFocus()
            return
        } else {
            binding.tilPassword.error = null
        }

        // Mulai proses Login ke Firebase
        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Selamat datang di Ludopoly!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan. Silakan daftar terlebih dahulu."
                        is FirebaseAuthInvalidCredentialsException -> "Email atau kata sandi salah."
                        is FirebaseNetworkException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                        else -> exception?.localizedMessage ?: "Gagal masuk. Silakan coba lagi."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnLogin.alpha = 0.6f
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            binding.btnLogin.alpha = 1.0f
        }
    }
}
