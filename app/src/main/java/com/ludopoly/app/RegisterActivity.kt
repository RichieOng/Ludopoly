package com.ludopoly.app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.ludopoly.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        // Tombol Daftar
        binding.btnRegister.setOnClickListener {
            handleRegister()
        }

        // Teks Masuk Akun
        binding.tvLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun handleRegister() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validasi Nama
        if (name.isEmpty()) {
            binding.tilName.error = "Nama pemain wajib diisi"
            binding.etName.requestFocus()
            return
        } else {
            binding.tilName.error = null
        }

        // Validasi Email
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

        // Validasi Password
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

        // Validasi Konfirmasi Password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Ulangi kata sandi Anda"
            binding.etConfirmPassword.requestFocus()
            return
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Kata sandi tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return
        } else {
            binding.tilConfirmPassword.error = null
        }

        // Mulai Pendaftaran Firebase
        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    // Perbarui nama profil pengguna di Firebase Auth
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    firebaseUser?.updateProfile(profileUpdates)

                    // Simpan data awal ke Firestore di background (tidak memblokir UI)
                    if (firebaseUser != null) {
                        saveUserToFirestore(firebaseUser.uid, name, email)
                    }

                    // Keluarkan sesi otomatis agar pengguna tetap harus login manual
                    auth.signOut()

                    setLoading(false)

                    // Tampilkan dialog informasi registrasi selesai
                    AlertDialog.Builder(this)
                        .setTitle("Registrasi Selesai")
                        .setMessage("Akun Ludopoly Anda berhasil didaftarkan!\nSilakan masuk menggunakan email dan kata sandi Anda.")
                        .setPositiveButton("Masuk Sekarang") { _, _ ->
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.putExtra("EXTRA_EMAIL", email)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    setLoading(false)
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthUserCollisionException -> "Email ini sudah terdaftar. Gunakan email lain atau silakan masuk."
                        is FirebaseAuthWeakPasswordException -> "Kata sandi terlalu lemah. Gunakan kombinasi huruf dan angka."
                        is FirebaseNetworkException -> "Koneksi internet bermasalah. Periksa jaringan Anda."
                        else -> exception?.localizedMessage ?: "Pendaftaran gagal. Silakan coba lagi."
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        try {
            val userMap = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email,
                "level" to 1,
                "coins" to 1000,
                "wins" to 0,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener {
                    // Berhasil tersimpan di Firestore
                }
                .addOnFailureListener {
                    // Gagal/belum diaktifkan, tidak apa-apa karena akun Auth sudah berhasil
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false
            binding.btnRegister.alpha = 0.6f
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true
            binding.btnRegister.alpha = 1.0f
        }
    }
}
