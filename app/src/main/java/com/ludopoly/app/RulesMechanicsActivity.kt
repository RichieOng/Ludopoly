package com.ludopoly.app

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.ludopoly.app.databinding.ActivityRulesMechanicsBinding

class RulesMechanicsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRulesMechanicsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesMechanicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSimulateBlackhole.setOnClickListener {
            simulateBlackhole()
        }

        binding.btnSimulateCapture.setOnClickListener {
            simulateCapture()
        }
    }

    private fun simulateBlackhole() {
        val islands = listOf("Pulau Sumatera", "Pulau Jawa", "Pulau Kalimantan", "Pulau Sulawesi", "Pulau Papua")
        val randomIsland = islands.random()

        AlertDialog.Builder(this)
            .setTitle("🌀 Blackhole Aktif!")
            .setMessage("Gawat! Pion Anda menginjak petak Blackhole di persimpangan selat!\n\nPion tersedot ke pusaran dimensi dan terlempar tiba di: [$randomIsland].")
            .setPositiveButton("Lanjutkan Langkah", null)
            .show()
    }

    private fun simulateCapture() {
        AlertDialog.Builder(this)
            .setTitle("⚔️ Pion Lawan Tertangkap!")
            .setMessage("HEBAT! Pion Anda berhasil menginjak petak lawan.\n\n• Pion musuh dipulangkan paksa ke pangkalan Rumah Adatnya.\n• Anda mendapatkan 1x kocokan dadu ekstra!")
            .setPositiveButton("Kocok Dadu Lagi", null)
            .show()
    }
}
