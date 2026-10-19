package com.ludopoly.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ludopoly.app.databinding.ActivityGameArenaBinding
import java.util.Random

class GameArenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameArenaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val random = Random()
    private var currentStep = 4
    private var currentRound = 1
    private val maxSteps = 20
    private var playerCoins: Long = 1000

    private val tiles = listOf(
        "Pangkalan Start (Rumah Gadang)",
        "Kota Padang (Sumatera)",
        "Selat Sunda (Petak Laut)",
        "Kota Jakarta (Jawa)",
        "Kota Bandung (Rumah Joglo)",
        "Kuis Budaya Nusantara",
        "Kota Yogyakarta (Jawa)",
        "Kota Surabaya (Jawa)",
        "Selat Madura (Petak Laut)",
        "🌀 BLACKHOLE TELEPORTASI",
        "Kota Pontianak (Rumah Betang)",
        "Kota Banjarmasin (Kalimantan)",
        "Selat Makassar (Petak Laut)",
        "Kota Makassar (Rumah Tongkonan)",
        "Kuis Budaya Nusantara",
        "Kota Manado (Sulawesi)",
        "Laut Banda (Petak Laut)",
        "Kota Jayapura (Rumah Honai)",
        "Kota Merauke (Papua)",
        "🏁 PUSAT NUSANTARA (IKN)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameArenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    playerCoins = doc.getLong("coins") ?: 1000
                    binding.tvArenaCoins.text = String.format("%,d LP", playerCoins)
                }
            }
    }

    private fun setupListeners() {
        binding.btnArenaRoll.setOnClickListener {
            rollAndMove()
        }

        binding.btnArenaNfc.setOnClickListener {
            val intent = Intent(this, NfcCityActivity::class.java)
            startActivity(intent)
        }

        binding.btnArenaQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }
    }

    private fun rollAndMove() {
        val diceValue = random.nextInt(6) + 1
        updateDiceFace(diceValue)

        currentStep = (currentStep + diceValue) % maxSteps
        binding.progressCircuit.progress = currentStep

        val destinationTile = tiles[currentStep]
        binding.tvArenaDiceStatus.text = "Dadu: Angka $diceValue"
        binding.tvCurrentIsland.text = "Posisi Pion: $destinationTile"
        binding.tvCircuitStep.text = "Petak $currentStep dari $maxSteps petak sirkuit Nusantara"

        // Cek peristiwa petak
        checkTileEvent(destinationTile, diceValue)
    }

    private fun checkTileEvent(tileName: String, diceValue: Int) {
        val logEntry = "\n• Dadu: $diceValue ➔ Maju ke [$tileName]"
        val oldLog = binding.tvGameLog.text.toString()
        binding.tvGameLog.text = "$oldLog$logEntry"

        when {
            tileName.contains("BLACKHOLE") -> {
                currentStep = (currentStep + 5) % maxSteps
                binding.progressCircuit.progress = currentStep
                val newDest = tiles[currentStep]

                AlertDialog.Builder(this)
                    .setTitle("🌀 Terkena Blackhole!")
                    .setMessage("Pion Anda menginjak lubang dimensi Blackhole!\nPion terlempar teleportasi ke: $newDest.")
                    .setPositiveButton("Lanjutkan", null)
                    .show()
            }
            tileName.contains("Kuis") -> {
                Toast.makeText(this, "Petak Kuis Budaya! Buka menu Kuis untuk bonus koin.", Toast.LENGTH_SHORT).show()
            }
            tileName.contains("PUSAT NUSANTARA") -> {
                AlertDialog.Builder(this)
                    .setTitle("🏆 FINIS NUSANTARA!")
                    .setMessage("Selamat! Pion Anda telah mencapai garis akhir di Pusat Nusantara (IKN)!")
                    .setPositiveButton("Hebat", null)
                    .show()
            }
        }
    }

    private fun updateDiceFace(value: Int) {
        val drawableRes = when (value) {
            1 -> R.drawable.ic_dice_1
            2 -> R.drawable.ic_dice_2
            3 -> R.drawable.ic_dice_3
            4 -> R.drawable.ic_dice_4
            5 -> R.drawable.ic_dice_5
            else -> R.drawable.ic_dice_6
        }
        binding.ivArenaDice.setImageResource(drawableRes)
    }
}
