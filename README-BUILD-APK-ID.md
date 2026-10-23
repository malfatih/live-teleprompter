# Membuat APK Live Teleprompter lewat GitHub Actions (tanpa Android Studio)

## Yang dibutuhkan
- Akun GitHub.
- Browser dan koneksi internet.
- File proyek ini.

## Langkah
1. Masuk ke https://github.com dan buat repository baru, misalnya `live-teleprompter-android`.
2. Unggah seluruh isi folder proyek ini ke repository (bukan ZIP-nya). Pastikan folder `.github/workflows` ikut terunggah.
3. Buka tab **Actions** di repository. Jika diminta, aktifkan GitHub Actions.
4. Pilih workflow **Build Android APK**, lalu klik **Run workflow**.
5. Tunggu proses selesai. Jika gagal, buka job build dan baca bagian log merah/error.
6. Jika berhasil, buka hasil run workflow, bagian **Artifacts**, lalu unduh `live-teleprompter-debug-apk`.
7. Ekstrak ZIP artifact dan salin `app-debug.apk` ke HP Android untuk dipasang.

## Catatan penting
- Workflow ini membuat **debug APK** untuk uji coba, bukan APK release bertanda tangan milik Anda.
- Build cloud memerlukan akses internet untuk mengunduh Gradle, Android Gradle Plugin, Android SDK dan dependensi.
- Keberhasilan kompilasi belum membuktikan semua fungsi bekerja sempurna. Uji overlay, penyimpanan naskah, izin kamera/mikrofon, rekaman, dan kompatibilitas pada HP Android Anda.
- Jika build gagal, unduh atau salin log error dari GitHub Actions dan minta bantuan untuk memperbaiki sumbernya.
- Jangan masukkan kata sandi, token, atau file keystore pribadi ke repository publik.
