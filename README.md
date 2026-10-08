# BLE Proximity Tracker

Aplikasi Android native untuk mendeteksi perangkat Bluetooth Low Energy (BLE), memantau fluktuasi nilai RSSI secara real-time, dan melacak kedekatan perangkat target menggunakan visualisasi radar interaktif serta penyimpanan lokal Room Database.

---

## Build Artifact (File APK)

Aplikasi telah dikompilasi ke versi release siap pasang dengan konfigurasi signing release dan optimasi R8 / ProGuard:
- **Lokasi file di repositori:** `BleTracker-v1.0.0.apk` (tersedia di root project)
- **Tautan Download (GitHub Release):** [Unduh BleTracker-v1.0.0.apk](https://github.com/Jizzyyy/ble-tracker/releases/download/v1.0.0/BleTracker-v1.0.0.apk)

---

## Panduan Setup & Kompilasi

### Prasyarat Lingkungan
- **JDK:** OpenJDK 17
- **Android SDK:** Platform SDK 35 (Android 15), Min SDK 26 (Android 8.0 Oreo)
- **Build Tools:** 35.0.0
- **Gradle:** Wrapper 8.13

### Menjalankan Unit Test
Pengujian unit memverifikasi akurasi pemetaan zona RSSI, kalkulasi formula estimasi jarak, dan peredaman fluktuasi sinyal:
```bash
./gradlew testDebugUnitTest
```

### Kompilasi File APK Release
Untuk menghasilkan file APK release yang ditandatangani dan dioptimasi:
```bash
./gradlew assembleRelease
```
File biner berekstensi `.apk` akan dihasilkan di direktori:
`app/build/outputs/apk/release/app-release.apk`

---

## Arsitektur Aplikasi

Proyek ini dibangun menggunakan **Clean Architecture** yang dipadukan dengan pola **MVVM (Model-View-ViewModel)** dengan pemisahan concern yang tegas:

```
app/src/main/java/com/bletracker/
├── data/
│   ├── ble/              # Wrapper native BluetoothLeScanner berbasis callbackFlow
│   ├── local/            # SQLite Room Database, Entity (DeviceEntity), dan DAO
│   └── repository/       # Implementasi konkrit BleRepositoryImpl & HistoryRepositoryImpl
├── domain/
│   ├── model/            # Model domain murni (BleDevice, SignalZone)
│   ├── repository/       # Interface abstraksi domain (BleRepository, HistoryRepository)
│   └── util/             # Perhitungan fisika sinyal & filter noise (RssiUtil)
├── di/                   # Definisi modul Dependency Injection Koin
└── ui/
    ├── components/       # Komponen visual (DeviceCard, SignalBadge, Shimmer, PermissionHandler)
    ├── navigation/       # NavHost Compose & rute navigasi aplikasi
    ├── scanner/          # Layar Dashboard Scanner & ScannerViewModel
    ├── radar/            # Layar Radar Tracking, Canvas rendering, & RadarViewModel
    ├── history/          # Layar Riwayat Perangkat & HistoryViewModel
    └── theme/            # Tema Light Mode, Color palette, dan tipografi Inter
```

### Pemilihan Teknologi & Alasan Teknis

1. **Kotlin 2.0 & Jetpack Compose:**
   - **Alasan:** Memungkinkan pembangunan UI deklaratif secara reaktif tanpa fragment/XML. Animasi pemindaian radar dan pulsing blip dirender langsung pada `Canvas` (`drawCircle`, `drawArc`), menjamin performa render 60 fps tanpa memicu recomposition berlebihan.
2. **Koin 3.5 (Dependency Injection):**
   - **Alasan:** Konfigurasi injeksi dependensi berbasis DSL Kotlin yang bersih dan ringkas. Tidak membutuhkan waktu tambahan untuk pemrosesan anotasi (KSP/kapt) saat kompilasi seperti Dagger/Hilt, sehingga build time jauh lebih efisien.
3. **Room Database 2.6:**
   - **Alasan:** Abstraksi SQLite resmi dari Jetpack yang menyediakan compile-time query verification dan terintegrasi langsung dengan Kotlin Flow untuk menyajikan pembaruan data lokal secara reaktif.
4. **Kotlin Coroutines & Flow (`callbackFlow`):**
   - **Alasan:** `BluetoothLeScanner` native Android bekerja berbasis asynchronous callback. Membungkusnya menggunakan `callbackFlow` memungkinkan konversi stream paket sinyal menjadi Flow reaktif yang otomatis membersihkan callback (`stopScan`) ketika lifecycle observer ditutup via `awaitClose`.

---

## Pemetaan Kategori Sinyal & Formula Estimasi

Pemetaan kategori sinyal diatur mengikuti spesifikasi teknis berikut:

| Nilai RSSI (dBm) | Kategori Sinyal | Perkiraan Jarak (Meter) | Indikator Visual |
| :--- | :--- | :--- | :--- |
| **-10 s/d -30 dBm** | Sangat Kuat (Sangat Dekat) | < 1 meter | Hijau Emerald |
| **-30 s/d -50 dBm** | Kuat (Dekat) | 1 – 3 meter | Hijau |
| **-50 s/d -70 dBm** | Cukup / Baik | 3 – 10 meter | Kuning Amber |
| **-70 s/d -80 dBm** | Lemah | 10 – 20 meter | Oranye |
| **-80 s/d -90 dBm** | Sangat Lemah / Putus-putus | > 20 meter (Batas jangkauan) | Merah |
| **< -90 dBm** | Sinyal Hilang (Lost) | Terputus / Di luar jangkauan | Abu-abu Slate |

### Asumsi Teknis & Formula

1. **Log-Distance Path Loss Model:**
   Perkiraan jarak matematis dikalkulasikan melalui formula propagasi radio:
   $$d = 10^{\left(\frac{TxPower - RSSI}{10 \times n}\right)}$$
   - $TxPower$: Nilai daya pancar referensi pada jarak 1 meter (dikalibrasi pada nilai $-30\text{ dBm}$).
   - $n$: Konstanta eksponen redaman jalur (*path loss exponent*) lingkungan dalam ruangan (ditetapkan sebesar $3.6$).

2. **Exponential Moving Average (EMA):**
   Sinyal RSSI mentah di lingkungan nyata berfluktuasi akibat pantulan fisik (*multipath fading*). Formula EMA diterapkan untuk menghaluskan fluktuasi:
   $$RSSI_{smoothed} = (\alpha \times RSSI_{raw}) + ((1 - \alpha) \times RSSI_{prev})$$
   dengan koefisien bobot $\alpha = 0.35$.

---

## Penanganan Edge Case & Lifecycle

- **Manajemen Izin Bertingkat:** Menangani runtime permissions untuk Android 12+ (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`) serta Android 11 ke bawah (`ACCESS_FINE_LOCATION` dan validasi status GPS aktif).
- **Deteksi Hardware Dinamis:** `BroadcastReceiver` memantau `BluetoothAdapter.ACTION_STATE_CHANGED` secara instan, sehingga jika Bluetooth dimatikan oleh pengguna saat aplikasi berjalan, UI langsung merespons tanpa jeda.
- **Konservasi Daya (App Lifecycle):** `LifecycleEventObserver` otomatis menghentikan proses scanning ketika aplikasi masuk ke latar belakang (`ON_STOP`) untuk mencegah pemborosan baterai.
- **Isolasi Mode Riwayat:** Membuka detail perangkat dari tab riwayat tidak memicu scanning hardware otomatis, menjaga efisiensi daya dan hanya menampilkan snapshot data terakhir tersimpan.

---

## Keterbatasan Aplikasi (Known Issues)

1. **Fluktuasi Fisik Frekuensi 2.4 GHz:**
   Nilai RSSI sangat dipengaruhi oleh interferensi gelombang Wi-Fi, perabot ruangan, dan penyerapan oleh tubuh manusia. Oleh karena itu, estimasi jarak bersifat perkiraan probabilitas kekuatan sinyal, bukan penentuan koordinat spasial absolut.
2. **Interval Advertising Perangkat:**
   Pembaruan data telemetri bergantung penuh pada seberapa sering perangkat target memancarkan paket advert (beberapa beacon memancarkan tiap 100 ms, sementara perangkat pasif lainnya memancarkan paket lebih jarang).
3. **Optimasi Baterai OS:**
   Sebagian vendor Android menerapkan kebijakan agresif yang dapat menunda penerimaan paket BLE jika sistem mendeteksi baterai berada dalam mode hemat daya.
