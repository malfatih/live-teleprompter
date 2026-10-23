# Live Teleprompter Android — Floating Live Assistant & Video Studio

Aplikasi Android profesional native yang membantu kreator konten membaca naskah berjalan saat melakukan siaran langsung (*live streaming*) di **TikTok Live, Instagram Live, Facebook Live, YouTube Live, Shopee Live**, sekaligus menyediakan **Studio Rekaman Video MP4** bawaan untuk merekam konten video dengan teleprompter dan mengunduhnya ke galeri ponsel.

---

## 🌟 Fitur Utama

### 1. Studio Rekam Video MP4 (Fitur Baru)
- **Kamera Depan & Belakang**: Rekam video HD langsung di dalam aplikasi sambil membaca naskah teleprompter yang bergulir otomatis.
- **Teks Tepat di Bawah Kamera**: Letak naskah dirancang tepat di bawah kamera depan ponsel sehingga tatapan mata Anda (*eye-contact*) terlihat 100% natural menghadap penonton.
- **Hitung Mundur (Countdown 3, 2, 1)**: Memberikan jeda persiapan sebelum proses perekaman video dimulai.
- **Format Video MP4 Standar**: Hasil rekaman langsung disimpan dalam format video **MP4 (H.264/AAC)** berdefinisi tinggi.
- **Download & Simpan ke Galeri**: Tombol **"Download ke Galeri"** menyalin file MP4 ke direktori publik ponsel (`Movies/LiveTeleprompter` via Android `MediaStore`), sehingga video langsung muncul di aplikasi Galeri, Google Photos, dan folder Unduhan/Downloads.
- **Bagikan Video MP4**: Fitur berbagi langsung via `FileProvider` ke WhatsApp, Telegram, Google Drive, TikTok, atau Instagram Reels.
- **Galeri Rekaman Video Terintegrasi**: Halaman khusus untuk melihat daftar video MP4 yang telah direkam, lengkap dengan ukuran file (MB), tanggal rekaman, tombol putar (*play*), unduh, dan hapus.

### 2. Floating Teleprompter (Jendela Mengambang di Atas Aplikasi Lain)
- **Overlay WindowManager**: Teks naskah mengambang di atas aplikasi media sosial menggunakan izin resmi Android `SYSTEM_ALERT_WINDOW`.
- **Drag & Reposition**: Geser bilah atas jendela untuk memindahkan posisi ke dekat lubang kamera depan ponsel.
- **Resize Handle**: Geser sudut kanan bawah untuk memperbesar atau memperkecil ukuran jendela secara dinamis.
- **Minimize Bubble**: Perkecil jendela menjadi ikon bundar (*floating bubble*) saat ingin fokus menunjukkan produk.
- **Pengaturan Transparansi (Opacity)**: Latar belakang jendela dapat diatur transparansinya (20% – 95%) agar komentar live dan ekspresi wajah tetap terlihat jelas di bawah naskah.
- **Kontrol Play / Pause / Rewind / Fast Forward**: Kendalikan jalannya teks dengan kontrol sekali sentuh atau langsung melalui panel notifikasi persisten (*Foreground Service*).

### 3. Editor Naskah Cerdas & Penyimpanan Lokal (Room Database)
- **100% Offline & Privat**: Naskah disimpan di database SQLite lokal perangkat menggunakan Android Room. Tidak ada data yang dikirim ke server luar, dan aplikasi tidak pernah meminta kredensial media sosial.
- **Hitung Kata & Estimasi Durasi**: Menghitung jumlah kata dan waktu bicara otomatis (standar 130 kata per menit).
- **Template Snippet Cepat**: Tombol cepat untuk menambahkan Sapaan Awal, Hook Flash Sale, Call-to-Action Keranjang Kuning, dan Poin Penting.
- **Impor & Tempel Teks**: Mendukung penyalinan teks dari catatan eksternal maupun file TXT.
- **5 Contoh Naskah Siap Pakai Bawaan**:
  1. *Live Jualan Flash Sale & Promo Spesial*
  2. *Affiliate Review Skincare & Serum Glowing*
  3. *Edukasi 3 Rahasia Konsisten Ngonten*
  4. *Webinar & Talkshow Pembukaan Sesi*
  5. *Promosi & Giveaway Tap-Tap Layar*

### 4. Mode Layar Penuh (Full-Screen Prompter)
- Untuk latihan membaca sebelum live atau digunakan pada smartphone kedua sebagai kaca teleprompter studio.
- **Mirror Mode (Horizontal Flip)**: Membalik teks secara cermin untuk penggunaan reflektor kaca teleprompter.

### 5. Mode Split Screen & Panduan Layar Terbagi
- Panduan langkah demi langkah untuk mengaktifkan fitur Split Screen Android pada berbagai merk HP (Samsung, Xiaomi, Oppo/Realme, Vivo, Pixel).
- Simulator *Dual View* dalam aplikasi untuk melatih kontak mata.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa**: Kotlin (100%)
- **UI Framework**: Jetpack Compose dengan Material 3 Design
- **Kamera & Video**: Android Jetpack CameraX (`camera-camera2`, `camera-lifecycle`, `camera-view`, `camera-video`)
- **Penyimpanan Video**: Android `MediaStore` & `FileProvider` untuk ekspor/unduh MP4 ke galeri
- **Window Management**: Android `WindowManager` dengan `TYPE_APPLICATION_OVERLAY`
- **Background Task**: Android `ForegroundService` bertipe `specialUse` dengan *ongoing notification*
- **Database Lokal**: Android Room Database (`AppDatabase`, `ScriptDao`, `ScriptEntity`)
- **Penyimpanan Preferensi**: Android Jetpack DataStore Preferences

---

## 📹 Cara Merekam Video & Mengunduh File MP4

1. Di Dashboard, ketuk tombol hijau **"Rekam MP4"** atau kartu **"Rekam Video MP4"**.
2. Berikan izin Kamera dan Mikrofon saat diminta.
3. Kamera depan akan aktif dengan teks naskah mengambang di atasnya.
4. Anda dapat mengatur kecepatan scroll teks (`+` / `−`) atau ukuran font (`Aa`).
5. Ketuk tombol bulat merah besar di bagian bawah layar untuk mulai merekam.
6. Hitung mundur **3, 2, 1** akan muncul, lalu video mulai direkam dan naskah bergulir otomatis.
7. Setelah selesai membaca naskah, ketuk tombol kotak merah (**■**) untuk menghentikan rekaman.
8. Dialog konfirmasi akan langsung muncul dengan nama file video (contoh: `Teleprompter_20261022_153012.mp4`):
   - Ketuk **"Download ke Galeri"**: File MP4 langsung disalin ke folder Galeri / Movies ponsel Anda.
   - Ketuk **"Bagikan"**: Langsung kirimkan file MP4 ke WhatsApp, Telegram, TikTok, atau Instagram.
   - Ketuk **"Putar"**: Langsung melihat pratinjau hasil rekaman video.
9. Anda juga dapat membuka menu **"Galeri Video MP4"** kapan saja untuk melihat, memutar, dan mengunduh seluruh rekaman video MP4 yang telah dibuat.
