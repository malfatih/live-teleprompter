package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCyan
import com.example.ui.theme.LiveGold
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val colorOptions = listOf(
        "#FFFFFF" to "Putih Bersih",
        "#FFEB3B" to "Kuning Terang",
        "#00E5FF" to "Cyan Neon",
        "#76FF03" to "Hijau Terang"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan Teleprompter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.resetSettings()
                        Toast.makeText(context, "Pengaturan direset ke awal", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = LiveCrimson
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = Color(0xFF0B0E19)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Box
            item {
                Text(
                    text = "Pratinjau Teks & Transparansi:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Color.Black.copy(alpha = settings.backgroundOpacity)
                        )
                        .border(1.dp, LiveCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Halo semuanya! Selamat datang di Live!",
                            color = try {
                                Color(android.graphics.Color.parseColor(settings.textColorHex))
                            } catch (_: Exception) {
                                Color.White
                            },
                            fontSize = (settings.fontSizeSp * 0.9f).sp,
                            lineHeight = (settings.fontSizeSp * 1.35f).sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Transparansi ${(settings.backgroundOpacity * 100).toInt()}% • Kecepatan ${settings.scrollSpeed.toInt()}x",
                            color = LiveGold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // 1. Scroll Speed Slider
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kecepatan Bergulir (Scroll Speed)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${settings.scrollSpeed.toInt()}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiveCyan)
                    }
                    Slider(
                        value = settings.scrollSpeed,
                        onValueChange = { viewModel.updateScrollSpeed(it) },
                        valueRange = 1.0f..10.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = LiveCyan, activeTrackColor = LiveCyan),
                        modifier = Modifier.testTag("slider_scroll_speed")
                    )
                    Text("Pilih kecepatan membaca yang santai dan nyaman saat menyapa penonton.", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // 2. Font Size Slider
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ukuran Teks Naskah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${settings.fontSizeSp.toInt()} sp", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiveGold)
                    }
                    Slider(
                        value = settings.fontSizeSp,
                        onValueChange = { viewModel.updateFontSize(it) },
                        valueRange = 14.0f..36.0f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = LiveGold, activeTrackColor = LiveGold)
                    )
                    Text("Gunakan ukuran teks lebih besar jika ponsel berada di atas tripod agak jauh.", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // 3. Background Opacity Slider
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transparansi Latar Jendela", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${(settings.backgroundOpacity * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LiveCrimson)
                    }
                    Slider(
                        value = settings.backgroundOpacity,
                        onValueChange = { viewModel.updateBackgroundOpacity(it) },
                        valueRange = 0.20f..0.95f,
                        colors = SliderDefaults.colors(thumbColor = LiveCrimson, activeTrackColor = LiveCrimson)
                    )
                    Text("Semakin transparan, semakin mudah melihat ekspresi wajah dan komentar penonton live.", fontSize = 11.sp, color = TextSecondary)
                }
            }

            // 4. Text Color Picker
            item {
                SettingCard {
                    Text("Warna Teks Naskah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { (hex, name) ->
                            val isSelected = settings.textColorHex.equals(hex, ignoreCase = true)
                            val color = Color(android.graphics.Color.parseColor(hex))

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) LiveCyan else DarkOutline,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateTextColor(hex) }
                            )
                        }
                    }
                }
            }

            // 5. Mirror Mode & Screen Awake Switches
            item {
                SettingCard {
                    // Mirror Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mode Kaca (Mirror Flip)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Membalik teks secara horizontal jika Anda menggunakan aksesoris kaca teleprompter atau cermin.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.isMirrorMode,
                            onCheckedChange = { viewModel.updateMirrorMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiveCyan, checkedTrackColor = LiveCyan.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Keep Screen Awake
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jaga Layar Tetap Menyala", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Mencegah layar redup atau terkunci saat live streaming sedang berlangsung.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.keepScreenOn,
                            onCheckedChange = { viewModel.updateKeepScreenOn(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiveGreen, checkedTrackColor = LiveGreen.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // 6. Voice Following (Speech Recognition) Toggle & Notice
            item {
                SettingCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = LiveViolet, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Voice-Following (Eksperimental)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Text("Teks maju otomatis mendeteksi ucapan suara Anda.", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = settings.voiceFollowingEnabled,
                            onCheckedChange = { viewModel.updateVoiceFollowing(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LiveViolet, checkedTrackColor = LiveViolet.copy(alpha = 0.4f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0x337C4DFF)
                    ) {
                        Text(
                            text = "Catatan: Di beberapa tipe ponsel Android, mikrofon dikunci eksklusif oleh aplikasi live (TikTok/IG). Jika suara tidak terdeteksi, teleprompter otomatis tetap berjalan dalam mode gulir waktu.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // 7. Privacy & Security Statement
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10192E)),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privasi & Keamanan 100% Offline", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Seluruh naskah Anda tersimpan secara lokal di database perangkat (Room).\n" +
                                    "• Aplikasi tidak pernah meminta password akun media sosial mana pun.\n" +
                                    "• Tanpa iklan, tanpa pelacakan data sensitif, dan tidak membutuhkan koneksi internet untuk membaca naskah.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Reset Button
            item {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        Toast.makeText(context, "Pengaturan diatur ulang ke bawaan", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23192B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = LiveCrimson, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kembalikan Pengaturan ke Bawaan", color = LiveCrimson, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = { content() }
        )
    }
}
