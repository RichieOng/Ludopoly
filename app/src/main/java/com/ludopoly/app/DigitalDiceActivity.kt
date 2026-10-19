package com.ludopoly.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.RotateAnimation
import androidx.appcompat.app.AppCompatActivity
import com.ludopoly.app.databinding.ActivityDigitalDiceBinding
import java.util.Random

class DigitalDiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDigitalDiceBinding
    private val random = Random()
    private val historyList = mutableListOf<Int>()
    private var isRolling = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDigitalDiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRollDice.setOnClickListener {
            if (!isRolling) {
                rollDice()
            }
        }
    }

    private fun rollDice() {
        isRolling = true
        binding.btnRollDice.isEnabled = false
        binding.btnRollDice.alpha = 0.6f
        binding.tvDiceResult.text = "Mengocok dadu..."
        binding.tvDiceInstruction.text = "Dadu berputar di papan sirkuit Nusantara..."

        // Animasi putaran dadu
        val rotate = RotateAnimation(
            0f, 360f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        rotate.duration = 400
        rotate.repeatCount = 1
        binding.ivDice.startAnimation(rotate)

        // Efek transisi cepat angka dadu (800ms)
        val handler = Handler(Looper.getMainLooper())
        var rollCount = 0
        val maxRollCount = 10

        val runnable = object : Runnable {
            override fun run() {
                if (rollCount < maxRollCount) {
                    val tempValue = random.nextInt(6) + 1
                    updateDiceImage(tempValue)
                    rollCount++
                    handler.postDelayed(this, 80)
                } else {
                    // Hasil akhir dadu (1 - 6)
                    val finalValue = random.nextInt(6) + 1
                    updateDiceImage(finalValue)
                    isRolling = false
                    binding.btnRollDice.isEnabled = true
                    binding.btnRollDice.alpha = 1.0f

                    historyList.add(0, finalValue)
                    if (historyList.size > 8) historyList.removeAt(historyList.size - 1)

                    displayFinalResult(finalValue)
                }
            }
        }
        handler.post(runnable)
    }

    private fun displayFinalResult(value: Int) {
        binding.tvDiceResult.text = "Hasil: Angka $value!"

        if (value == 6) {
            binding.tvDiceInstruction.text = "🌟 LUAR BIASA! Anda dapat mengeluarkan pion dari Rumah Adat atau melempar dadu 1x lagi!"
        } else {
            binding.tvDiceInstruction.text = "Pion Anda melangkah maju $value petak di jalur sirkuit pulau."
        }

        // Tampilkan riwayat
        binding.tvDiceHistory.text = historyList.joinToString(" ➔ ") { "[$it]" }
    }

    private fun updateDiceImage(value: Int) {
        val drawableRes = when (value) {
            1 -> R.drawable.ic_dice_1
            2 -> R.drawable.ic_dice_2
            3 -> R.drawable.ic_dice_3
            4 -> R.drawable.ic_dice_4
            5 -> R.drawable.ic_dice_5
            else -> R.drawable.ic_dice_6
        }
        binding.ivDice.setImageResource(drawableRes)
    }
}
