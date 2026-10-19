package com.ludopoly.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ludopoly.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user == null) {
            // Jika sesi hilang, arahkan kembali ke Login
            redirectToLogin()
            return
        }

        val displayName = user.displayName ?: "Pemain Ludopoly"
        val email = user.email ?: "Tidak ada email"
        val uid = user.uid

        binding.tvUserName.text = displayName
        binding.tvUserEmail.text = email

        // Mengambil statistik pemain dari Cloud Firestore
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val level = document.getLong("level") ?: 1
                    val coins = document.getLong("coins") ?: 1000
                    val wins = document.getLong("wins") ?: 0

                    binding.tvLevel.text = level.toString()
                    binding.tvCoins.text = String.format("%,d LP", coins)
                    binding.tvWins.text = "$wins Game"

                    val nameFromDb = document.getString("name")
                    if (!nameFromDb.isNullOrEmpty()) {
                        binding.tvUserName.text = nameFromDb
                    }
                }
            }
            .addOnFailureListener {
                // Fallback jika Firestore belum diaktifkan atau offline
            }
    }

    override fun onResume() {
        super.onResume()
        // Muat ulang saldo koin dan statistik pemain terbaru
        loadUserData()
    }

    private fun setupListeners() {
        // Tombol Mulai Main (Play Game -> Masuk ke Lobby Room Multiplayer)
        binding.btnPlayGame.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
        }

        // Fitur 1: Lima Pulau dan Rumah Adat
        binding.menuIslands.setOnClickListener {
            val intent = Intent(this, IslandsActivity::class.java)
            startActivity(intent)
        }

        // Fitur 2: Dadu Digital
        binding.menuDice.setOnClickListener {
            val intent = Intent(this, DigitalDiceActivity::class.java)
            startActivity(intent)
        }

        // Fitur 3: Kuis Pengetahuan Nusantara
        binding.menuQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }

        // Fitur 4: Kartu NFC dan Pembelian Kota
        binding.menuNfcCity.setOnClickListener {
            val intent = Intent(this, NfcCityActivity::class.java)
            startActivity(intent)
        }

        // Fitur 5: Blackhole, Pion Tertangkap, dan Sistem Kemenangan
        binding.menuRules.setOnClickListener {
            val intent = Intent(this, RulesMechanicsActivity::class.java)
            startActivity(intent)
        }

        // Tombol Keluar Akun (Logout)
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Akun")
            .setMessage("Apakah Anda yakin ingin keluar dari Ludopoly?")
            .setPositiveButton("Ya, Keluar") { _, _ ->
                auth.signOut()
                Toast.makeText(this, "Berhasil keluar dari akun.", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
