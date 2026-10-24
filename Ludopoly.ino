
#include <WiFi.h>
#include "secrets.h"
#include <Firebase.h>

// =====================================
// LUDOPOLY NUSANTARA - FIREBASE
// =====================================

Firebase fb(REFERENCE_URL);

String deviceID = "ESP32_LudoPoly";

unsigned long waktuSebelumnya = 0;
const unsigned long intervalUpdate = 5000;

// Deklarasi fungsi
void hubungkanWiFi();
void kirimDataAwal();
void kirimDataPulau();
void kirimDataPetakKhusus();
void perbaruiStatus();
void cekResponse(String nama, int kode);
void setStringData(String path, String nilai);
void setIntData(String path, int nilai);

// =====================================
// SETUP
// =====================================

void setup() {
    Serial.begin(115200);
    delay(1000);

    Serial.println();
    Serial.println("==================================");
    Serial.println("       LUDOPOLY NUSANTARA");
    Serial.println("==================================");

    hubungkanWiFi();

    if (WiFi.status() == WL_CONNECTED) {
        kirimDataAwal();
        kirimDataPulau();
        kirimDataPetakKhusus();

        Serial.println();
        Serial.println("Inisialisasi data LudoPoly selesai.");
    } else {
        Serial.println("WiFi belum terhubung.");
        Serial.println("Data belum dikirim ke Firebase.");
    }
}

// =====================================
// LOOP
// =====================================

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi terputus. Mencoba menghubungkan ulang...");
        WiFi.reconnect();
        delay(1000);
        return;
    }

    if (millis() - waktuSebelumnya >= intervalUpdate) {
        waktuSebelumnya = millis();
        perbaruiStatus();
    }
}

// =====================================
// WIFI
// =====================================

void hubungkanWiFi() {
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    Serial.print("Menghubungkan ke WiFi");

    unsigned long waktuMulai = millis();

    while (WiFi.status() != WL_CONNECTED &&
           millis() - waktuMulai < 30000) {
        Serial.print(".");
        delay(500);
    }

    Serial.println();

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("WiFi berhasil terhubung!");
        Serial.print("IP Address: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println("Gagal terhubung ke WiFi.");
    }
}

// =====================================
// DATA AWAL PERMAINAN
// =====================================

void kirimDataAwal() {
    Serial.println();
    Serial.println("Mengirim data perangkat dan permainan...");

    setStringData("LudoPoly/IoT/deviceID", deviceID);
    setStringData("LudoPoly/IoT/deviceStatus", "online");
    setStringData("LudoPoly/IoT/rfidStatus", "belum_dipasang");

    setStringData("LudoPoly/Permainan/status", "belum_dimulai");
    setIntData("LudoPoly/Permainan/jumlahPemain", 4);
    setIntData("LudoPoly/Permainan/putaran", 1);
    setIntData("LudoPoly/Permainan/giliranPemain", 1);

    setStringData(
        "LudoPoly/Permainan/namaPermainan",
        "LudoPoly Nusantara"
    );
}

// =====================================
// DATA KOTA DAN PULAU
// Harga disimpan dalam rupiah
// =====================================

void kirimDataPulau() {
    Serial.println();
    Serial.println("Mengirim data pulau dan kota...");

    // PULAU JAWA - RUMAH JOGLO
    setStringData("LudoPoly/Kota/Jawa/rumahAdat", "Joglo");

    setStringData("LudoPoly/Kota/Jawa/Jakarta/nama", "Jakarta");
    setIntData("LudoPoly/Kota/Jawa/Jakarta/harga", 200000);

    setStringData("LudoPoly/Kota/Jawa/Yogyakarta/nama", "Yogyakarta");
    setIntData("LudoPoly/Kota/Jawa/Yogyakarta/harga", 100000);

    setStringData("LudoPoly/Kota/Jawa/Bandung/nama", "Bandung");
    setIntData("LudoPoly/Kota/Jawa/Bandung/harga", 100000);

    setStringData("LudoPoly/Kota/Jawa/Surabaya/nama", "Surabaya");
    setIntData("LudoPoly/Kota/Jawa/Surabaya/harga", 100000);


    // PULAU SUMATERA - RUMAH GADANG
    setStringData("LudoPoly/Kota/Sumatera/rumahAdat", "Rumah Gadang");

    setStringData("LudoPoly/Kota/Sumatera/Padang/nama", "Padang");
    setIntData("LudoPoly/Kota/Sumatera/Padang/harga", 100000);

    setStringData("LudoPoly/Kota/Sumatera/Palembang/nama", "Palembang");
    setIntData("LudoPoly/Kota/Sumatera/Palembang/harga", 100000);

    setStringData("LudoPoly/Kota/Sumatera/Medan/nama", "Medan");
    setIntData("LudoPoly/Kota/Sumatera/Medan/harga", 200000);

    setStringData("LudoPoly/Kota/Sumatera/BandaAceh/nama", "Banda Aceh");
    setIntData("LudoPoly/Kota/Sumatera/BandaAceh/harga", 100000);


    // PULAU KALIMANTAN - RUMAH BETANG
    setStringData("LudoPoly/Kota/Kalimantan/rumahAdat", "Rumah Betang");

    setStringData("LudoPoly/Kota/Kalimantan/PalangkaRaya/nama", "Palangka Raya");
    setIntData("LudoPoly/Kota/Kalimantan/PalangkaRaya/harga", 100000);

    setStringData("LudoPoly/Kota/Kalimantan/Samarinda/nama", "Samarinda");
    setIntData("LudoPoly/Kota/Kalimantan/Samarinda/harga", 100000);

    setStringData("LudoPoly/Kota/Kalimantan/Pontianak/nama", "Pontianak");
    setIntData("LudoPoly/Kota/Kalimantan/Pontianak/harga", 200000);

    setStringData("LudoPoly/Kota/Kalimantan/Banjarmasin/nama", "Banjarmasin");
    setIntData("LudoPoly/Kota/Kalimantan/Banjarmasin/harga", 100000);


    // PULAU SULAWESI - RUMAH TONGKONAN
    setStringData("LudoPoly/Kota/Sulawesi/rumahAdat", "Tongkonan");

    setStringData("LudoPoly/Kota/Sulawesi/Manado/nama", "Manado");
    setIntData("LudoPoly/Kota/Sulawesi/Manado/harga", 100000);

    setStringData("LudoPoly/Kota/Sulawesi/Palu/nama", "Palu");
    setIntData("LudoPoly/Kota/Sulawesi/Palu/harga", 100000);

    setStringData("LudoPoly/Kota/Sulawesi/Kendari/nama", "Kendari");
    setIntData("LudoPoly/Kota/Sulawesi/Kendari/harga", 100000);

    setStringData("LudoPoly/Kota/Sulawesi/Makassar/nama", "Makassar");
    setIntData("LudoPoly/Kota/Sulawesi/Makassar/harga", 200000);


    // PULAU PAPUA - RUMAH HONAI
    setStringData("LudoPoly/Kota/Papua/rumahAdat", "Honai");

    setStringData("LudoPoly/Kota/Papua/Merauke/nama", "Merauke");
    setIntData("LudoPoly/Kota/Papua/Merauke/harga", 100000);

    setStringData("LudoPoly/Kota/Papua/Jayapura/nama", "Jayapura");
    setIntData("LudoPoly/Kota/Papua/Jayapura/harga", 200000);

    setStringData("LudoPoly/Kota/Papua/Sorong/nama", "Sorong");
    setIntData("LudoPoly/Kota/Papua/Sorong/harga", 100000);

    setStringData("LudoPoly/Kota/Papua/Biak/nama", "Biak");
    setIntData("LudoPoly/Kota/Papua/Biak/harga", 100000);

    Serial.println("Data pulau dan kota selesai dikirim.");
}

// =====================================
// DATA PETAK KHUSUS
// =====================================

void kirimDataPetakKhusus() {
    Serial.println();
    Serial.println("Mengirim data petak khusus...");

    setStringData(
        "LudoPoly/PetakKhusus/Kuis/nama",
        "Ada Apa, Indonesia?"
    );
    setStringData(
        "LudoPoly/PetakKhusus/Kuis/status",
        "aktif"
    );

    setStringData(
        "LudoPoly/PetakKhusus/DanaWarga/nama",
        "Dana Warga"
    );
    setStringData(
        "LudoPoly/PetakKhusus/DanaWarga/status",
        "aktif"
    );

    setStringData(
        "LudoPoly/PetakKhusus/Pajak/nama",
        "Pajak"
    );
    setStringData(
        "LudoPoly/PetakKhusus/Pajak/status",
        "aktif"
    );

    setStringData(
        "LudoPoly/PetakKhusus/JalurGaib/nama",
        "Jalur Gaib"
    );
    setStringData(
        "LudoPoly/PetakKhusus/JalurGaib/status",
        "aktif"
    );

    setStringData(
        "LudoPoly/PetakKhusus/LaluLintas/nama",
        "Lalu Lintas"
    );
    setStringData(
        "LudoPoly/PetakKhusus/LaluLintas/status",
        "aktif"
    );

    Serial.println("Data petak khusus selesai dikirim.");
}

// =====================================
// UPDATE STATUS PERANGKAT
// =====================================

void perbaruiStatus() {
    int responseCode = fb.setString(
        "LudoPoly/IoT/deviceStatus",
        "online"
    );

    cekResponse("Update Device Status", responseCode);
}

// =====================================
// FUNGSI FIREBASE
// =====================================

void setStringData(String path, String nilai) {
    int responseCode = fb.setString(path, nilai);
    cekResponse(path, responseCode);
}

void setIntData(String path, int nilai) {
    int responseCode = fb.setInt(path, nilai);
    cekResponse(path, responseCode);
}

void cekResponse(String nama, int kode) {
    Serial.print(nama);
    Serial.print(" | Response Code: ");
    Serial.println(kode);

    if (kode == 200) {
        Serial.println("Berhasil dikirim.");
    } else {
        Serial.println("Gagal dikirim. Periksa konfigurasi Firebase.");
    }
}
