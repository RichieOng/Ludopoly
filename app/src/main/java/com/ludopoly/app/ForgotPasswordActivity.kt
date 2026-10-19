package com.ludopoly.app

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.ludopoly.app.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnResetPassword.setOnClickListener {
            handleResetPassword()
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleResetPassword() {
        val email = binding.etEmail.text.toString().trim()

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

        setLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    AlertDialog.Builder(this)
                        .setTitle("Tautan Terkirim")
                        .setMessage("Instruksi reset kata sandi telah dikirim ke $email. Silakan periksa kotak masuk atau folder spam email Anda.")
                        .setPositiveButton("Kembali ke Login") { _, _ ->
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthInvalidUserException -> "Email ini belum terdaftar di Ludopoly."
                        is FirebaseNetworkException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                        else -> exception?.localizedMessage ?: "Gagal mengirim link reset. Silakan coba lagi."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnResetPassword.isEnabled = false
            binding.btnResetPassword.alpha = 0.6f
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnResetPassword.isEnabled = true
            binding.btnResetPassword.alpha = 1.0f
        }
    }
}
