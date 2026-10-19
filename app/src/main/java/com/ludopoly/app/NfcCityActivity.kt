package com.ludopoly.app

import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.ludopoly.app.databinding.ActivityNfcCityBinding

data class CityProperty(
    val id: String,
    val name: String,
    val island: String,
    val price: Long,
    val rent: Long,
    var isOwned: Boolean = false
)

class NfcCityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNfcCityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var nfcAdapter: NfcAdapter? = null
    private var currentCoins: Long = 1000

    private val cityList = mutableListOf(
        CityProperty("c1", "Kota Padang", "Pulau Sumatera", 400, 100),
        CityProperty("c2", "Kota Bandung", "Pulau Jawa", 600, 150),
        CityProperty("c3", "Kota Balikpapan", "Pulau Kalimantan", 500, 120),
        CityProperty("c4", "Kota Makassar", "Pulau Sulawesi", 550, 130),
        CityProperty("c5", "Kota Jayapura", "Pulau Papua", 700, 180)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNfcCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnBack.setOnClickListener {
            finish()
        }

        checkNfcAvailability()
        loadPlayerCoins()
        renderCityList()

        binding.btnSimulateNfc.setOnClickListener {
            simulateNfcTap()
        }
    }

    private fun checkNfcAvailability() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            binding.tvNfcStatusTitle.text = "NFC Tidak Tersedia di Perangkat"
            binding.tvNfcStatusDesc.text = "Gunakan tombol 'SIMULASI TAP' di samping untuk mencoba scan kartu NFC."
        } else if (!nfcAdapter!!.isEnabled) {
            binding.tvNfcStatusTitle.text = "NFC Dinonaktifkan"
            binding.tvNfcStatusDesc.text = "Silakan aktifkan NFC di pengaturan ponsel Anda atau gunakan tombol Simulasi."
        } else {
            binding.tvNfcStatusTitle.text = "Sensor NFC Aktif"
            binding.tvNfcStatusDesc.text = "Tempelkan kartu fisik NFC LudoPoly ke sensor belakang HP Anda."
        }
    }

    private fun loadPlayerCoins() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    currentCoins = doc.getLong("coins") ?: 1000
                    binding.tvPlayerCoins.text = String.format("%,d LP", currentCoins)
                }
            }
    }

    private fun renderCityList() {
        binding.containerCities.removeAllViews()

        for (city in cityList) {
            val cardView = layoutInflater.inflate(R.layout.item_city_card, binding.containerCities, false)

            val tvName = cardView.findViewById<TextView>(R.id.tvCityName)
            val tvIsland = cardView.findViewById<TextView>(R.id.tvCityIsland)
            val tvPrice = cardView.findViewById<TextView>(R.id.tvCityPrice)
            val tvRent = cardView.findViewById<TextView>(R.id.tvCityRent)
            val tvStatus = cardView.findViewById<TextView>(R.id.tvCityStatus)
            val btnAction = cardView.findViewById<AppCompatButton>(R.id.btnBuyCity)

            tvName.text = city.name
            tvIsland.text = city.island
            tvPrice.text = "Harga Beli: ${city.price} LP"
            tvRent.text = "Biaya Sewa: ${city.rent} LP"

            if (city.isOwned) {
                tvStatus.text = "TERBELI / MILIK ANDA"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.color_success))
                btnAction.text = "KOTA DIMILIKI"
                btnAction.isEnabled = false
                btnAction.alpha = 0.5f
            } else {
                tvStatus.text = "BELUM DIMILIKI"
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                btnAction.text = "BELI DENGAN NFC (${city.price} LP)"
                btnAction.isEnabled = true
                btnAction.alpha = 1.0f

                btnAction.setOnClickListener {
                    promptBuyCity(city)
                }
            }

            binding.containerCities.addView(cardView)
        }
    }

    private fun promptBuyCity(city: CityProperty) {
        if (currentCoins < city.price) {
            Toast.makeText(this, "Saldo koin Anda tidak cukup (${currentCoins} LP). Jawab kuis untuk menambah koin!", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Pembelian Kota via NFC")
            .setMessage("Apakah Anda ingin membeli ${city.name} (${city.island}) seharga ${city.price} LP?")
            .setPositiveButton("Beli Sekarang") { _, _ ->
                executeCityPurchase(city)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun executeCityPurchase(city: CityProperty) {
        currentCoins -= city.price
        city.isOwned = true
        binding.tvPlayerCoins.text = String.format("%,d LP", currentCoins)

        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid)
                .update("coins", currentCoins)
        }

        renderCityList()

        AlertDialog.Builder(this)
            .setTitle("🎉 Pembelian Berhasil!")
            .setMessage("Selamat! ${city.name} kini menjadi wilayah kekuasaan Anda. Pemain lawan yang singgah harus membayar sewa sebesar ${city.rent} LP kepada Anda.")
            .setPositiveButton("Mantap", null)
            .show()
    }

    private fun simulateNfcTap() {
        val unownedCities = cityList.filter { !it.isOwned }
        if (unownedCities.isEmpty()) {
            Toast.makeText(this, "Semua kota di 5 pulau sudah Anda miliki!", Toast.LENGTH_SHORT).show()
            return
        }

        val targetCity = unownedCities.random()
        AlertDialog.Builder(this)
            .setTitle("💳 Kartu NFC Terdeteksi!")
            .setMessage("Sensor NFC membaca kartu: [${targetCity.name} - ${targetCity.island}].\nHarga: ${targetCity.price} LP | Sewa: ${targetCity.rent} LP.\n\nBeli kota ini sekarang?")
            .setPositiveButton("Beli") { _, _ ->
                promptBuyCity(targetCity)
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
}
