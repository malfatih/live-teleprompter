package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ScriptDao
import com.example.data.model.ScriptEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ScriptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "live_teleprompter_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialScripts(database.scriptDao())
                    }
                }
            }

            private suspend fun populateInitialScripts(dao: ScriptDao) {
                dao.insertScript(
                    ScriptEntity(
                        title = "Live Jualan: Flash Sale & Promo Spesial",
                        category = "Live Jualan",
                        content = """Halo semuanya! Selamat datang di live streaming kita hari ini!
Boleh bantu tap-tap layar dulu ya temen-temen sampai 10k likes!
Jangan lupa follow akun kita biar gak ketinggalan voucher diskon khusus followers.

Yang baru gabung absen dulu dari kota mana aja nih? Tulis di kolom komentar ya!

Oke guys, hari ini kita lagi ada PROMO SPESIAL FLASH SALE terbatas cuma 30 menit ke depan!
Barang yang ada di etalase nomor 1 lagi diskon 50% plus gratis ongkir ke seluruh Indonesia.

Kalian langsung klik keranjang kuning di pojok kiri bawah sekarang juga!
Jangan sampai kehabisan karena stok diskon ini cuma ada 50 paket aja.

Siapa yang mau spill etalase nomor 2? Komen 'MAU' di kolom chat sekarang!
Yang paling gercep checkout bakal dapet bonus mystery box eksklusif dari kita.
Ayo checkout sekarang sebelum vouchernya hangus!""",
                        isFavorite = true
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "Affiliate: Review Skincare & Serum Glowing",
                        category = "Affiliate",
                        content = """Hai bestie! Banyak banget yang nanya ke aku di DM:
"Kak, rahasia kulit wajah glowing gak kusam selama seharian apa sih?"

Nah, hari ini aku spill langsung di live ini!
Ini dia produk andalan aku selama 2 bulan terakhir: Serum Niacinamide Glowing!

Teksturnya itu ringan banget, cepat meresap, dan sama sekali gak lengket di kulit.
Bisa bantu memudarkan noda bekas jerawat, meratakan warna kulit, dan bikin kulit kenyal.
Sudah BPOM dan aman untuk kulit sensitif ya guys!

Khusus yang checkout di live hari ini, kalian dapet potongan harga Rp 40.000 + Buy 1 Get 1 free mini moisturizer!
Cek keranjang kuning etalase nomor 3 sekarang juga ya sebelum kehabisan!""",
                        isFavorite = true
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "Edukasi: 3 Rahasia Konsisten Ngonten Setiap Hari",
                        category = "Edukasi",
                        content = """Halo creators! Pernah gak ngerasa bingung mau bikin konten apa besok?
Atau udah semangat di awal, tapi baru seminggu udah burn out?

Tenang, hari ini saya akan bagikan 3 rahasia praktis agar kamu bisa konsisten ngonten tanpa kehabisan ide:

1. Batching Content: Jangan buat konten satu per satu tiap hari. Luangkan 1 hari khusus (misal hari Minggu) untuk rekam 5 sampai 7 video sekaligus!
2. Content Pillar: Bagi tema konten kamu jadi 3 pilar utama: Edukasi, Hiburan, dan Cerita Pribadi. Ini bikin ide kamu terstruktur rapi.
3. Repurpose: Ubah satu ide video panjang jadi cuplikan video pendek, postingan teks, dan topik diskusi di live streaming!

Catat poin-poin ini ya! Ada pertanyaan? Tulis di kolom komentar, kita bahas bareng-bareng sekarang!""",
                        isFavorite = false
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "Webinar & Talkshow: Pembukaan Sesi",
                        category = "Webinar",
                        content = """Selamat malam rekan-rekan peserta yang terhormat.
Terima kasih telah bergabung dalam sesi Virtual Sharing Session malam ini.

Sebelum kita mulai, pastikan koneksi internet Anda stabil, dan sediakan catatan untuk mencatat insight penting dari narasumber kita.
Acara kita malam ini akan terbagi ke dalam 3 sesi utama:
Pertama: Pemaparan materi inti selama 35 menit.
Kedua: Studi kasus implementasi praktis.
Ketiga: Sesi tanya jawab (Q&A) langsung dengan narasumber.

Silakan ajukan pertanyaan rekan-rekan melalui kolom komentar dengan format:
[NAMA] - [ASAL KOTA] - [PERTANYAAN].
Mari kita sambut narasumber kita malam hari ini!""",
                        isFavorite = false
                    )
                )

                dao.insertScript(
                    ScriptEntity(
                        title = "Promosi & Giveaway: Tap Layar & Share",
                        category = "Promosi",
                        content = """HALO GUYS! Siapa yang mau dapet hadiah GIVEAWAY saldo e-wallet dan produk gratis malam ini?!

Syaratnya gampang banget dan GRATIS:
1. Tap-tap layar kalian bareng-bareng sampai target 50.000 likes tercapai!
2. Klik tombol share dan bagikan live ini ke 3 temen atau grup WhatsApp kalian.
3. Tulis komentar: 'SEMANGAT LIVE' sebanyak-banyaknya!

Host bakal pilih 3 pemenang beruntung yang paling aktif di kolom chat setiap kelipatan 10k likes!
Ayo ajak temen-temen kalian masuk sekarang juga! Let's go tap tap layarnya!""",
                        isFavorite = false
                    )
                )
            }
        }
    }
}
