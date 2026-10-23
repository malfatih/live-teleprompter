package com.example.ui.splitscreen

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreenGuideScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scripts by viewModel.allScripts.collectAsState()
    val activeScript = scripts.firstOrNull()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Panduan, 1: Simulasi Latihan

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mode Split Screen",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = Color(0xFF0B0E19)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                contentColor = LiveCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LiveCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Panduan Perangkat", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Simulasi Dual View", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                SplitScreenGuideTabContent(
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )
            } else {
                SplitScreenPracticeSimulator(activeScript = activeScript)
            }
        }
    }
}

@Composable
fun SplitScreenGuideTabContent(onOpenSettings: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Concept Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LiveCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerticalSplit,
                            contentDescription = null,
                            tint = LiveCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Konsep Split Screen",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Bagi layar jadi 2: Bagian atas untuk Live Teleprompter, bagian bawah untuk kamera atau aplikasi live streaming.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Brand-specific Step by Step Guide
        item {
            Text(
                text = "Cara Mengaktifkan di Perangkat Anda:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            BrandGuideCard(
                brand = "Samsung Galaxy (One UI)",
                steps = listOf(
                    "Buka Live Teleprompter terlebih dahulu.",
                    "Tekan tombol Recent Apps (atau swipe up lalu tahan jika pakai gesture).",
                    "Ketuk ikon aplikasi Live Teleprompter di atas jendela pratinjau.",
                    "Pilih 'Buka dalam tampilan layar terbagi' (Open in split screen view).",
                    "Pilih aplikasi kamera atau media sosial di bagian bawah layar."
                )
            )
        }

        item {
            BrandGuideCard(
                brand = "Xiaomi, POCO, Redmi (MIUI / HyperOS)",
                steps = listOf(
                    "Buka Recent Apps.",
                    "Tekan dan tahan jendela Live Teleprompter.",
                    "Pilih ikon dua persegi bertumpuk (Layar Terbagi / Split Screen).",
                    "Buka aplikasi live streaming Anda di layar kedua."
                )
            )
        }

        item {
            BrandGuideCard(
                brand = "Oppo, Realme, Vivo & Lainnya",
                steps = listOf(
                    "Usap ke atas dengan 3 jari (fitur 3-finger split screen jika aktif di Pengaturan).",
                    "Atau buka Recent Apps > ketuk titik tiga di pojok kartu > Layar Terbagi."
                )
            )
        }

        // Platform Limitations Disclaimer
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, LiveGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                color = Color(0xFF221E12)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Perhatian",
                        tint = LiveGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Catatan Penting Kompatibilitas:",
                            color = LiveGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beberapa aplikasi seperti Instagram atau kamera bawaan pabrik terkadang melarang mode split screen native oleh sistem Android.\n\n" +
                                    "✨ Solusi Terbaik: Gunakan 'Mode Floating Teleprompter' (fitur utama aplikasi ini) yang dapat mengambang di atas aplikasi apa pun tanpa batasan split screen!",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Shortcut Button
        item {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = LiveCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buka Pengaturan Sistem Android", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BrandGuideCard(brand: String, steps: List<String>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        color = DarkSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = brand,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LiveCyan
            )
            Spacer(modifier = Modifier.height(8.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}. ",
                        color = LiveGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = step,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SplitScreenPracticeSimulator(activeScript: com.example.data.model.ScriptEntity?) {
    val scrollState = rememberScrollState()
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive && isPlaying) {
                val next = scrollState.value + 3
                if (next <= scrollState.maxValue) {
                    scrollState.scrollTo(next)
                } else {
                    isPlaying = false
                }
                delay(30L)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP HALF: Teleprompter Simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0C1022))
                .border(1.dp, LiveCyan.copy(alpha = 0.5f))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PANEL ATAS: TELEPROMPTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiveCyan
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Putar",
                                tint = LiveCrimson
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = activeScript?.content ?: "Halo semuanya! Selamat datang di live streaming kita hari ini! Tap tap layarnya ya!",
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Split Divider Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(LiveCyan)
        )

        // BOTTOM HALF: Simulated Camera / Live View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF141A2E))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = LiveCrimson,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "PANEL BAWAH: KAMERA / APLIKASI LIVE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Di sini tampilan kamera atau aplikasi TikTok/IG live Anda berjalan saat split screen diaktifkan di ponsel nyata.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
                )
            }
        }
    }
}
