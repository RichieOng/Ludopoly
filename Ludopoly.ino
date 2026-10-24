
#include <WiFi.h>
#include "secrets.h"
#include <Firebase.h>

// Firebase Realtime Database
Firebase fb(REFERENCE_URL);

// Identitas perangkat
String deviceID = "ESP32_LudoPoly";

// Interval update status: 5 detik
unsigned long waktuSebelumnya = 0;
const unsigned long interval = 5000;

// Fungsi
void hubungkanWiFi();
void kirimDataAwal();
void perbaruiStatus();
void tampilkanHasil(String namaData, int responseCode);

void setup() {
    Serial.begin(115200);
    delay(1000);

    Serial.println();
    Serial.println("==================================");
    Serial.println("          LUDOPOLY IOT");
    Serial.println("==================================");

    hubungkanWiFi();

    if (WiFi.status() == WL_CONNECTED) {
        kirimDataAwal();
    }

    Serial.println();
    Serial.println("Inisialisasi LudoPoly selesai.");
    Serial.println("RFID/NFC belum dipasang.");
}

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("WiFi terputus. Mencoba menghubungkan ulang...");
        WiFi.reconnect();
        delay(1000);
        return;
    }

    if (millis() - waktuSebelumnya >= interval) {
        waktuSebelumnya = millis();
        perbaruiStatus();
    }
}

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
        Serial.println("Periksa SSID dan password.");
    }
}

void kirimDataAwal() {
    Serial.println();
    Serial.println("Mengirim data awal ke Firebase...");

    int responseCode;

    responseCode = fb.setString(
        "LudoPoly/IoT/deviceID",
        deviceID
    );
    tampilkanHasil("Device ID", responseCode);

    responseCode = fb.setString(
        "LudoPoly/IoT/deviceStatus",
        "online"
    );
    tampilkanHasil("Device Status", responseCode);

    responseCode = fb.setString(
        "LudoPoly/IoT/rfidStatus",
        "belum_dipasang"
    );
    tampilkanHasil("RFID/NFC Status", responseCode);

    responseCode = fb.setString(
        "LudoPoly/PembelianKota/status",
        "menunggu_kartu"
    );
    tampilkanHasil("Status Pembelian Kota", responseCode);

    responseCode = fb.setInt(
        "LudoPoly/Permainan/jumlahPemain",
        4
    );
    tampilkanHasil("Jumlah Pemain", responseCode);

    responseCode = fb.setInt(
        "LudoPoly/Permainan/putaran",
        1
    );
    tampilkanHasil("Putaran", responseCode);

    responseCode = fb.setString(
        "LudoPoly/Permainan/status",
        "belum_dimulai"
    );
    tampilkanHasil("Status Permainan", responseCode);

    responseCode = fb.setString(
        "LudoPoly/Permainan/jenisPermainan",
        "LudoPoly Nusantara"
    );
    tampilkanHasil("Jenis Permainan", responseCode);

    Serial.println();
    Serial.println("Pengiriman data awal selesai.");
}

void perbaruiStatus() {
    Serial.println();
    Serial.println("Memperbarui status perangkat...");

    int responseCode = fb.setString(
        "LudoPoly/IoT/deviceStatus",
        "online"
    );

    tampilkanHasil("Update Device Status", responseCode);
}

void tampilkanHasil(String namaData, int responseCode) {
    Serial.print(namaData);
    Serial.print(" - Response Code: ");
    Serial.println(responseCode);

    if (responseCode == 200) {
        Serial.println("Data berhasil dikirim.");
    } else {
        Serial.println("Pengiriman data gagal.");
        Serial.println("Periksa URL, koneksi, dan aturan Firebase.");
    }
}
