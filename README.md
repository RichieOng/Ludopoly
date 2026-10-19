# Ludopoly - Board Game Budaya Nusantara (Android Studio & Firebase)

Aplikasi Android **LudoPoly** — perpaduan permainan papan edukatif budaya Nusantara (Monopoli & Ludo), dilengkapi dengan Firebase Authentication, Cloud Firestore, sensor kartu NFC, kuis budaya, dan dadu digital.

---

## 📁 Lokasi Proyek
Direktori proyek:
`C:\Users\Asus\.gemini\antigravity\scratch\ludopoly`

---

## 🎮 Fitur-Fitur Utama Game LudoPoly

### 1. Halaman Utama (Main Dashboard)
- **Identitas Game LudoPoly**: Logo dadu, nama game, dan badge edisi Nusantara.
- **Profil Pemain & Saldo Koin**: Avatar, nama pemain, email, level pemain, dan saldo koin LP (Ludopoly Coins) yang tersinkronisasi dengan Firebase.
- **Tombol Play Game**: Membuka Arena Permainan LudoPoly.
- **Menu Fitur**: Akses cepat ke 5 fitur utama permainan.

### 2. List 5 Fitur Permainan
1. **Lima Pulau & Rumah Adat** (`IslandsActivity`):
   - Eksplorasi 5 pulau besar Nusantara:
     - **Sumatera**: Rumah Gadang (Minangkabau) & Kota Properti (Medan, Padang, Palembang).
     - **Jawa**: Rumah Joglo & Kota Properti (Jakarta, Bandung, Yogyakarta, Surabaya).
     - **Kalimantan**: Rumah Betang / Panjang (Dayak) & Kota Properti (Pontianak, Banjarmasin, Balikpapan).
     - **Sulawesi**: Rumah Tongkonan (Toraja) & Kota Properti (Makassar, Manado, Palu).
     - **Papua**: Rumah Honai & Kota Properti (Jayapura, Merauke, Sorong).
2. **Dadu Digital** (`DigitalDiceActivity`):
   - Animasi kocok dadu digital 1–6 dengan efek visual bergulir.
   - Aturan lemparan: Angka 6 untuk mengeluarkan pion dari Rumah Adat atau mendapatkan lemparan dadu tambahan.
   - Riwayat lemparan dadu interaktif.
3. **Kuis Pengetahuan Nusantara** (`QuizActivity`):
   - Kuis interaktif wawasan budaya, arsitektur rumah adat, dan sejarah kepulauan Indonesia.
   - Menjawab dengan benar memberikan bonus saldo koin (+200 LP per soal) yang otomatis bertambah ke akun Firebase pemain.
4. **Kartu NFC & Pembelian Kota** (`NfcCityActivity`):
   - Integrasi sensor NFC ponsel untuk memindai kartu fisik properti kota.
   - Dilengkapi tombol **Simulasi Tap NFC** untuk pengujian di emulator atau perangkat tanpa NFC.
   - Sistem kepemilikan kota, harga beli, dan penarikan biaya sewa koin saat pemain lawan mendarat.
5. **Blackhole, Pion Tertangkap & Sistem Kemenangan** (`RulesMechanicsActivity`):
   - **Blackhole**: Petak pusaran misterius yang menteleportasi pion ke pulau lain secara acak.
   - **Pion Tertangkap**: Mekanisme menginjak pion musuh agar kembali ke pangkalan Rumah Adat awal (+1x lemparan ekstra).
   - **Sistem Kemenangan**: Kondisi juara melalui Monopoli Kepulauan, Pion Finis ke IKN, atau Saldo Koin Tertinggi.
6. **Arena Permainan (Game Arena)** (`GameArenaActivity`):
   - Sirkuit 20 petak antarpulau, pelacak putaran ronde, kocokan dadu langkah langsung, dan pencatat pertandingan (Game Log).

---

## 🚀 Cara Menjalankan di Android Studio

1. Buka **Android Studio**.
2. Pilih **Open** > arahkan ke direktori:
   `C:\Users\Asus\.gemini\antigravity\scratch\ludopoly`
3. Tunggu proses **Gradle Sync** selesai.
4. Pastikan file konfigurasi `app/google-services.json` dari Firebase Console Anda sudah terpasang.
5. Jalankan ke emulator atau HP Android fisik (klik tombol **Run**).
