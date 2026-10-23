package com.example.ui.fullscreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCyan
import com.example.ui.theme.LiveGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun FullScreenPrompterScreen(
    scriptId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scripts by viewModel.allScripts.collectAsState()
    val activeScript = scripts.find { it.id == scriptId } ?: scripts.firstOrNull()

    val scrollState = rememberScrollState()
    var isPlaying by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(3.0f) }
    var fontSize by remember { mutableFloatStateOf(28.0f) }
    var isMirrorMode by remember { mutableStateOf(false) }
    var isCenterAligned by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }

    // Auto-scroll Coroutine loop
    LaunchedEffect(isPlaying, speed) {
        if (isPlaying) {
            while (isActive && isPlaying) {
                val step = (speed * 1.5f).toInt().coerceAtLeast(1)
                val target = scrollState.value + step
                if (target <= scrollState.maxValue) {
                    scrollState.scrollTo(target)
                } else {
                    isPlaying = false
                }
                delay(30L)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            }
    ) {
        // Scrolling Text Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    if (isMirrorMode) {
                        scaleX = -1f
                    }
                }
        ) {
            Spacer(modifier = Modifier.height(140.dp))

            Text(
                text = activeScript?.title ?: "Naskah Teleprompter",
                color = LiveCyan,
                fontSize = (fontSize * 0.75f).sp,
                fontWeight = FontWeight.Bold,
                textAlign = if (isCenterAligned) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = activeScript?.content ?: "Pilih naskah untuk membaca di mode layar penuh.",
                color = Color.White,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.45f).sp,
                textAlign = if (isCenterAligned) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(350.dp))
        }

        // Top Overlay Bar (Back, Title, Mirror, Align)
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0xCC11162B)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }

                    Text(
                        text = activeScript?.title ?: "Layar Penuh",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )

                    // Mirror Toggle
                    IconButton(onClick = { isMirrorMode = !isMirrorMode }) {
                        Icon(
                            Icons.Default.Flip,
                            contentDescription = "Mode Kaca / Mirror",
                            tint = if (isMirrorMode) LiveCyan else TextSecondary
                        )
                    }

                    // Alignment Toggle
                    IconButton(onClick = { isCenterAligned = !isCenterAligned }) {
                        Icon(
                            if (isCenterAligned) Icons.Default.FormatAlignCenter else Icons.Default.FormatAlignLeft,
                            contentDescription = "Rata Teks",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }

        // Bottom Controls Overlay Bar
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xE613192B)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress & Speed Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kecepatan (${speed.toInt()}x)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = speed,
                            onValueChange = { speed = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = LiveCyan,
                                activeTrackColor = LiveCyan
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind to top
                        IconButton(onClick = { scrollState.dispatchRawDelta(-scrollState.value.toFloat()) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Kembali ke Awal", tint = TextSecondary)
                        }

                        // Rewind 200px
                        IconButton(onClick = { scrollState.dispatchRawDelta(-200f) }) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Mundur", tint = Color.White)
                        }

                        // Big Play / Pause
                        Surface(
                            shape = CircleShape,
                            color = if (isPlaying) LiveCrimson else LiveCyan,
                            modifier = Modifier
                                .size(56.dp)
                                .clickable { isPlaying = !isPlaying }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Jeda" else "Putar",
                                    tint = if (isPlaying) Color.White else Color(0xFF00363D),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Forward 200px
                        IconButton(onClick = { scrollState.dispatchRawDelta(200f) }) {
                            Icon(Icons.Default.FastForward, contentDescription = "Maju", tint = Color.White)
                        }

                        // Font Size +/- toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E284A),
                            modifier = Modifier.clickable {
                                fontSize = when {
                                    fontSize < 24f -> 30f
                                    fontSize < 36f -> 42f
                                    else -> 22f
                                }
                            }
                        ) {
                            Text(
                                text = "Aa",
                                color = LiveGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
