package com.ludopoly.app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ludopoly.app.databinding.ActivityQuizBinding

data class QuizQuestion(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctIndex: Int, // 0 for A, 1 for B, 2 for C, 3 for D
    val explanation: String
)

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var currentIndex = 0
    private var totalEarnedCoins = 0
    private var hasAnsweredCurrent = false

    private val questions = listOf(
        QuizQuestion(
            question = "Rumah adat yang memiliki atap meruncing menyerupai tanduk kerbau bernama Rumah Gadang berasal dari pulau/daerah?",
            optionA = "A. Sumatera Barat (Minangkabau)",
            optionB = "B. Jawa Tengah",
            optionC = "C. Sulawesi Selatan",
            optionD = "D. Kalimantan Timur",
            correctIndex = 0,
            explanation = "Tepat! Rumah Gadang adalah rumah tradisional Minangkabau di Sumatera Barat dengan atap gonjong khas tanduk kerbau."
        ),
        QuizQuestion(
            question = "Apa nama tiang penyangga utama dari kayu jati yang menjadi ciri khas Rumah Joglo di Pulau Jawa?",
            optionA = "A. Tongkonan",
            optionB = "B. Saka Guru",
            optionC = "C. Honai",
            optionD = "D. Tumpangsari",
            correctIndex = 1,
            explanation = "Tepat! Saka Guru adalah 4 tiang penyangga utama yang menopang atap tumpangsari Rumah Joglo."
        ),
        QuizQuestion(
            question = "Rumah panggung tradisional Dayak di Kalimantan yang berukuran sangat panjang dan dihuni puluhan keluarga adalah?",
            optionA = "A. Rumah Gadang",
            optionB = "B. Rumah Honai",
            optionC = "C. Rumah Betang",
            optionD = "D. Rumah Kebaya",
            correctIndex = 2,
            explanation = "Benar! Rumah Betang (Rumah Panjang) melambangkan semangat kebersamaan dan gotong royong suku Dayak."
        ),
        QuizQuestion(
            question = "Rumah Tongkonan yang atapnya menyerupai haluan perahu leluhur dan dihiasi tanduk kerbau berasal dari suku?",
            optionA = "A. Suku Bugis",
            optionB = "B. Suku Toraja (Sulawesi Selatan)",
            optionC = "C. Suku Asmat",
            optionD = "D. Suku Batak",
            correctIndex = 1,
            explanation = "Benar! Rumah Tongkonan adalah rumah adat masyarakat suku Toraja di Sulawesi Selatan."
        ),
        QuizQuestion(
            question = "Di Pulau Papua, rumah adat berdinding kayu bundar beratap jerami/rumput tanpa jendela dinamakan?",
            optionA = "A. Rumah Honai",
            optionB = "B. Rumah Sasak",
            optionC = "C. Rumah Lamin",
            optionD = "D. Rumah Baileo",
            correctIndex = 0,
            explanation = "Hebat! Rumah Honai dirancang khusus tanpa jendela untuk menjaga kehangatan dari suhu dingin pegunungan Papua."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupOptionListeners()
        loadQuestion(currentIndex)
    }

    private fun setupOptionListeners() {
        binding.btnOptionA.setOnClickListener { checkAnswer(0) }
        binding.btnOptionB.setOnClickListener { checkAnswer(1) }
        binding.btnOptionC.setOnClickListener { checkAnswer(2) }
        binding.btnOptionD.setOnClickListener { checkAnswer(3) }

        binding.btnNextQuestion.setOnClickListener {
            currentIndex++
            if (currentIndex < questions.size) {
                loadQuestion(currentIndex)
            } else {
                showQuizFinishedDialog()
            }
        }
    }

    private fun loadQuestion(index: Int) {
        hasAnsweredCurrent = false
        val q = questions[index]

        binding.tvQuestionProgress.text = "Soal ${index + 1} dari ${questions.size}"
        binding.tvQuestionText.text = q.question
        binding.btnOptionA.text = q.optionA
        binding.btnOptionB.text = q.optionB
        binding.btnOptionC.text = q.optionC
        binding.btnOptionD.text = q.optionD

        // Reset visual tombol
        resetOptionButtons()

        binding.tvFeedback.visibility = View.GONE
        binding.btnNextQuestion.visibility = View.GONE
    }

    private fun checkAnswer(selectedIndex: Int) {
        if (hasAnsweredCurrent) return
        hasAnsweredCurrent = true

        val q = questions[currentIndex]
        val buttons = listOf(binding.btnOptionA, binding.btnOptionB, binding.btnOptionC, binding.btnOptionD)

        if (selectedIndex == q.correctIndex) {
            // Benar
            buttons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.color_success))
            buttons[selectedIndex].setTextColor(Color.WHITE)
            totalEarnedCoins += 200
            binding.tvEarnedCoins.text = "+$totalEarnedCoins LP"
            binding.tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.color_success))
            binding.tvFeedback.text = "🎉 JAWABAN BENAR! (+200 Koin LP)\n${q.explanation}"
            updateUserCoinsInFirebase(200)
        } else {
            // Salah
            buttons[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.color_danger))
            buttons[selectedIndex].setTextColor(Color.WHITE)
            buttons[q.correctIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.color_success))
            buttons[q.correctIndex].setTextColor(Color.WHITE)
            binding.tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.color_danger))
            binding.tvFeedback.text = "❌ JAWABAN KURANG TEPAT!\n${q.explanation}"
        }

        binding.tvFeedback.visibility = View.VISIBLE
        binding.btnNextQuestion.visibility = View.VISIBLE
    }

    private fun resetOptionButtons() {
        val buttons = listOf(binding.btnOptionA, binding.btnOptionB, binding.btnOptionC, binding.btnOptionD)
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.bg_input)
            btn.setTextColor(ContextCompat.getColor(this, R.color.text_white))
        }
    }

    private fun updateUserCoinsInFirebase(coinsToAdd: Int) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("coins", FieldValue.increment(coinsToAdd.toLong()))
            .addOnSuccessListener {
                // Koin bertambah di Firestore
            }
            .addOnFailureListener {
                // Fallback offline
            }
    }

    private fun showQuizFinishedDialog() {
        AlertDialog.Builder(this)
            .setTitle("🏆 Kuis Selesai!")
            .setMessage("Luar biasa! Anda telah menyelesaikan Kuis Pengetahuan Nusantara dan mengumpulkan total hadiah: +$totalEarnedCoins Koin LP.")
            .setPositiveButton("Selesai & Kembali") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
