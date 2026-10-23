package com.example.ui.dashboard

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScriptEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.OverlayPermissionBanner
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCyan
import com.example.ui.theme.LiveGold
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.util.PermissionHelper
import com.example.util.ScriptStatsUtil

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToFullScreen: (Long) -> Unit,
    onNavigateToRecord: (Long) -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToSplitScreen: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scripts by viewModel.allScripts.collectAsState()
    val isFloatingRunning by viewModel.isFloatingServiceRunning.collectAsState()
    val activeTitle by viewModel.activeFloatingTitle.collectAsState()
    val isPlaying by viewModel.isPrompterPlaying.collectAsState()

    val hasOverlayPermission = PermissionHelper.canDrawOverlays(context)

    // Pulsing animation for Live dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0E19)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Header with LIVE Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(LiveCrimson)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LIVE TELEPROMPTER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiveCrimson,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Asisten & Studio Naskah",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = "Galeri Video MP4",
                            tint = LiveGreen
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = LiveCyan
                        )
                    }
                }
            }
        }

        // 2. Permission Banner
        item {
            OverlayPermissionBanner(
                hasPermission = hasOverlayPermission,
                onGrantClick = {
                    val intent = PermissionHelper.getOverlayPermissionIntent(context)
                    context.startActivity(intent)
                }
            )
        }

        // 3. Active Floating Status / Quick Launcher Card
        item {
            if (isFloatingRunning) {
                ActiveFloatingBanner(
                    title = activeTitle,
                    isPlaying = isPlaying,
                    onStop = { viewModel.stopFloating(context) },
                    onOpenEditor = {
                        val activeScript = scripts.find { it.title == activeTitle }
                        if (activeScript != null) onNavigateToEditor(activeScript.id)
                    }
                )
            } else {
                HeroStartCard(
                    scripts = scripts,
                    hasPermission = hasOverlayPermission,
                    onStartFloating = { script ->
                        if (hasOverlayPermission) {
                            viewModel.startFloating(context, script)
                        } else {
                            val intent = PermissionHelper.getOverlayPermissionIntent(context)
                            context.startActivity(intent)
                        }
                    },
                    onRecordVideo = { script ->
                        val id = script?.id ?: scripts.firstOrNull()?.id ?: 0L
                        onNavigateToRecord(id)
                    },
                    onCreateScript = { onNavigateToEditor(0) }
                )
            }
        }

        // 4. Quick Action Grid
        item {
            Text(
                text = "Fitur & Mode",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: Studio Rekam Video MP4 & Galeri Rekaman
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureActionCard(
                    title = "Rekam Video MP4",
                    subtitle = "Kamera + naskah gulir",
                    icon = Icons.Default.Videocam,
                    accentColor = LiveCrimson,
                    onClick = {
                        val firstId = scripts.firstOrNull()?.id ?: 0L
                        onNavigateToRecord(firstId)
                    },
                    modifier = Modifier.weight(1f)
                )
                FeatureActionCard(
                    title = "Galeri Video MP4",
                    subtitle = "Download & simpan video",
                    icon = Icons.Default.VideoLibrary,
                    accentColor = LiveGreen,
                    onClick = onNavigateToGallery,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Naskah Baru & Koleksi Naskah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureActionCard(
                    title = "Naskah Baru",
                    subtitle = "Tulis & atur teks",
                    icon = Icons.Default.Add,
                    accentColor = LiveCyan,
                    onClick = { onNavigateToEditor(0) },
                    modifier = Modifier.weight(1f)
                )
                FeatureActionCard(
                    title = "Daftar Naskah",
                    subtitle = "${scripts.size} naskah tersimpan",
                    icon = Icons.Default.Dashboard,
                    accentColor = LiveViolet,
                    onClick = onNavigateToList,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Layar Penuh & Split Screen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureActionCard(
                    title = "Layar Penuh",
                    subtitle = "Mode latihan & kaca",
                    icon = Icons.Default.Fullscreen,
                    accentColor = LiveGold,
                    onClick = {
                        val firstScriptId = scripts.firstOrNull()?.id ?: 0L
                        onNavigateToFullScreen(firstScriptId)
                    },
                    modifier = Modifier.weight(1f)
                )
                FeatureActionCard(
                    title = "Split Screen",
                    subtitle = "Panduan layar ganda",
                    icon = Icons.Default.VerticalSplit,
                    accentColor = LiveCyan,
                    onClick = onNavigateToSplitScreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Recent Scripts Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Naskah Pilihan Siap Live & Rekam",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Lihat Semua",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiveCyan,
                    modifier = Modifier.clickable { onNavigateToList() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (scripts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada naskah. Klik tombol 'Naskah Baru' di atas.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(scripts.take(5)) { script ->
                        RecentScriptCard(
                            script = script,
                            onStartFloating = {
                                if (hasOverlayPermission) {
                                    viewModel.startFloating(context, script)
                                } else {
                                    val intent = PermissionHelper.getOverlayPermissionIntent(context)
                                    context.startActivity(intent)
                                }
                            },
                            onRecord = { onNavigateToRecord(script.id) },
                            onEdit = { onNavigateToEditor(script.id) },
                            onFullScreen = { onNavigateToFullScreen(script.id) }
                        )
                    }
                }
            }
        }

        // 6. Pro Tips for Live Streamers & Video Recording
        item {
            StreamerTipsCard()
        }
    }
}

@Composable
fun ActiveFloatingBanner(
    title: String,
    isPlaying: Boolean,
    onStop: () -> Unit,
    onOpenEditor: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, LiveCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LiveCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "JENDELA FLOATING AKTIF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiveCyan
                    )
                }

                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Floating",
                        tint = LiveCrimson
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title.ifBlank { "Naskah Live" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Teleprompter sedang tampil di atas aplikasi lain. Anda bisa membuka TikTok, Instagram, atau Facebook sekarang.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tutup Jendela", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenEditor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Buka Editor", fontSize = 12.sp, color = LiveCyan)
                }
            }
        }
    }
}

@Composable
fun HeroStartCard(
    scripts: List<ScriptEntity>,
    hasPermission: Boolean,
    onStartFloating: (ScriptEntity) -> Unit,
    onRecordVideo: (ScriptEntity?) -> Unit,
    onCreateScript: () -> Unit
) {
    val topScript = scripts.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF1E284E),
                        Color(0xFF141933)
                    )
                )
            )
            .border(1.dp, Color(0xFF33406D), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mulai Siaran & Rekaman",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                CategoryBadge(category = "TikTok, IG & MP4")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (topScript != null) {
                Text(
                    text = "Naskah Siap: ${topScript.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiveCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val words = ScriptStatsUtil.countWords(topScript.content)
                val duration = ScriptStatsUtil.estimateDuration(words)
                Text(
                    text = "$words kata • Durasi estimasi $duration",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "Tulis naskah pertama Anda atau gunakan contoh naskah bawaan.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Floating Live Button
                Button(
                    onClick = {
                        if (topScript != null) {
                            onStartFloating(topScript)
                        } else {
                            onCreateScript()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCrimson),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_start_floating_live")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Mulai Floating",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (topScript != null) "Floating Live" else "Naskah Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Studio Rekam Video Button
                Button(
                    onClick = { onRecordVideo(topScript) },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_start_recording_studio")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Rekam Video MP4",
                        tint = Color(0xFF003314),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Rekam MP4",
                        color = Color(0xFF003314),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        color = DarkSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RecentScriptCard(
    script: ScriptEntity,
    onStartFloating: () -> Unit,
    onRecord: () -> Unit,
    onEdit: () -> Unit,
    onFullScreen: () -> Unit
) {
    val wordCount = ScriptStatsUtil.countWords(script.content)
    val duration = ScriptStatsUtil.estimateDuration(wordCount)

    Card(
        modifier = Modifier
            .width(270.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = script.category)
                if (script.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorit",
                        tint = LiveGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = script.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "$wordCount kata • Est. $duration",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            Text(
                text = script.content.take(80).replace("\n", " ") + "...",
                fontSize = 12.sp,
                color = TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onStartFloating,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("Live", color = Color(0xFF00363D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRecord,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCrimson),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Rekam", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp), tint = TextSecondary)
                }

                OutlinedButton(
                    onClick = onFullScreen,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = "Layar Penuh", modifier = Modifier.size(14.dp), tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun StreamerTipsCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DarkOutline, RoundedCornerShape(14.dp)),
        color = Color(0xFF13192B)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "💡 Tips Rekam Video & Live Streaming",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LiveGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. Gunakan 'Rekam Video MP4' untuk membuat konten video TikTok/Reels sambil membaca teks di depan kamera.\n" +
                        "2. Setelah selesai merekam, ketuk 'Download ke Galeri' agar video MP4 tersimpan langsung di galeri ponsel Anda.\n" +
                        "3. Gunakan 'Floating Live' saat Anda siaran langsung langsung di dalam aplikasi TikTok/Instagram.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
