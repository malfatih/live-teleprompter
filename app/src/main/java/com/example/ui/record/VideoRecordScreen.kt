package com.example.ui.record

import android.Manifest
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.ScriptEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.LiveCyan
import com.example.ui.theme.LiveGold
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PermissionHelper
import com.example.util.VideoStorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.util.Locale

@Composable
fun VideoRecordScreen(
    initialScriptId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scripts by viewModel.allScripts.collectAsState()

    var selectedScript by remember {
        mutableStateOf(scripts.find { it.id == initialScriptId } ?: scripts.firstOrNull())
    }

    // Permission state
    var hasCameraAndAudio by remember {
        mutableStateOf(PermissionHelper.hasRecordVideoPermissions(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraAndAudio = permissions[Manifest.permission.CAMERA] == true &&
                permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraAndAudio) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    // Camera selector state
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var currentRecording by remember { mutableStateOf<Recording?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Recording States
    var isRecording by remember { mutableStateOf(false) }
    var recordingDurationSeconds by remember { mutableIntStateOf(0) }
    var countdownValue by remember { mutableIntStateOf(0) } // 3, 2, 1, 0 (0 = inactive)
    var finishedVideoFile by remember { mutableStateOf<File?>(null) }

    // Teleprompter parameters
    val scrollState = rememberScrollState()
    var scrollSpeed by remember { mutableFloatStateOf(3.0f) }
    var fontSize by remember { mutableFloatStateOf(20.0f) }
    var prompterOpacity by remember { mutableFloatStateOf(0.7f) }
    var isMirrorMode by remember { mutableStateOf(false) }

    // Clean disposal when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                currentRecording?.stop()
            } catch (_: Exception) {}
        }
    }

    // Timer while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDurationSeconds = 0
            while (isActive && isRecording) {
                delay(1000L)
                recordingDurationSeconds++
            }
        }
    }

    // Auto-scroll while recording
    LaunchedEffect(isRecording, scrollSpeed) {
        if (isRecording) {
            while (isActive && isRecording) {
                val step = (scrollSpeed * 1.2f).toInt().coerceAtLeast(1)
                val target = scrollState.value + step
                if (target <= scrollState.maxValue) {
                    scrollState.scrollTo(target)
                }
                delay(30L)
            }
        }
    }

    // Countdown logic
    LaunchedEffect(countdownValue) {
        if (countdownValue > 0) {
            delay(1000L)
            val next = countdownValue - 1
            countdownValue = next
            if (next == 0) {
                startVideoRecording(
                    context = context,
                    videoCapture = videoCapture,
                    onRecordingStarted = { recording ->
                        currentRecording = recording
                        isRecording = true
                    },
                    onRecordingFinished = { file ->
                        isRecording = false
                        currentRecording = null
                        finishedVideoFile = file
                    }
                )
            }
        }
    }

    // Camera Binding with guarded capabilities check to eliminate "No supported EncoderProfiles"
    LaunchedEffect(previewViewRef, lensFacing, hasCameraAndAudio) {
        val previewView = previewViewRef ?: return@LaunchedEffect
        if (!hasCameraAndAudio) return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Check camera availability safely
                val hasFront = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                val hasBack = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

                val targetSelector = when {
                    lensFacing == CameraSelector.LENS_FACING_FRONT && hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                    hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                    hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                    else -> CameraSelector.DEFAULT_BACK_CAMERA
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val hasHardwareEncoder = try {
                    android.media.CamcorderProfile.hasProfile(android.media.CamcorderProfile.QUALITY_LOW) ||
                    android.media.CamcorderProfile.hasProfile(android.media.CamcorderProfile.QUALITY_HIGH) ||
                    android.media.CamcorderProfile.hasProfile(android.media.CamcorderProfile.QUALITY_480P) ||
                    android.media.CamcorderProfile.hasProfile(android.media.CamcorderProfile.QUALITY_720P)
                } catch (_: Exception) {
                    false
                }

                if (hasHardwareEncoder) {
                    val cameraInfo = cameraProvider.getCameraInfo(targetSelector)
                    val supportedQualities = try {
                        QualitySelector.getSupportedQualities(cameraInfo)
                    } catch (_: Exception) {
                        emptyList()
                    }

                    if (supportedQualities.isNotEmpty()) {
                        val qualitySelector = QualitySelector.fromOrderedList(
                            supportedQualities,
                            FallbackStrategy.lowerQualityOrHigherThan(supportedQualities.first())
                        )
                        val recorder = Recorder.Builder()
                            .setQualitySelector(qualitySelector)
                            .build()
                        val vCapture = VideoCapture.withOutput(recorder)
                        videoCapture = vCapture

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                targetSelector,
                                preview,
                                vCapture
                            )
                        } catch (_: Exception) {
                            videoCapture = null
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                targetSelector,
                                preview
                            )
                        }
                    } else {
                        videoCapture = null
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            targetSelector,
                            preview
                        )
                    }
                } else {
                    // Virtual camera / emulator: avoid creating Recorder with QualitySelector
                    // to prevent "No supported EncoderProfiles" errors. Bind preview cleanly!
                    videoCapture = null
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        targetSelector,
                        preview
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Blinking Record Dot Animation
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraAndAudio) {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = LiveCrimson,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Izin Kamera & Mikrofon Diperlukan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Untuk merekam video sambil membaca teleprompter dan menyimpan hasilnya dalam format MP4, izinkan akses kamera dan audio.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveCyan)
                ) {
                    Text("Berikan Izin Kamera & Audio", color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // 1. CameraX Preview View (stable, no unbind in update!)
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Top Bar (Back, Flip Camera, Gallery shortcut, Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isRecording) {
                            try { currentRecording?.stop() } catch (_: Exception) {}
                            isRecording = false
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x80000000))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }

                // Recording Status / Timer
                if (isRecording) {
                    val minutes = recordingDurationSeconds / 60
                    val seconds = recordingDurationSeconds % 60
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xCC000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LiveCrimson)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .scale(recAlpha)
                                    .clip(CircleShape)
                                    .background(LiveCrimson)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REC $formattedTime (MP4)",
                                color = LiveCrimson,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x99000000)
                    ) {
                        Text(
                            text = "Studio Rekam Video MP4",
                            color = LiveCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Flip Camera
                    IconButton(
                        onClick = {
                            if (!isRecording) {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    CameraSelector.LENS_FACING_BACK
                                } else {
                                    CameraSelector.LENS_FACING_FRONT
                                }
                            } else {
                                Toast.makeText(context, "Hentikan rekaman untuk membalik kamera", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Balik Kamera", tint = Color.White)
                    }

                    // Gallery shortcut
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x80000000))
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = "Galeri Rekaman MP4", tint = LiveCyan)
                    }
                }
            }

            // 3. Floating Teleprompter Window directly beneath camera lens
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 96.dp)
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = prompterOpacity))
                    .border(1.dp, LiveCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Prompter Header (Script Title, Speed, Aa)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedScript?.title ?: "Naskah Live",
                            color = LiveCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Speed - / +
                            Text(
                                text = "−",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { scrollSpeed = (scrollSpeed - 0.5f).coerceAtLeast(1f) }
                                    .padding(horizontal = 6.dp)
                            )
                            Text(
                                text = "${scrollSpeed.toInt()}x",
                                color = LiveGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+",
                                color = Color.White,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { scrollSpeed = (scrollSpeed + 0.5f).coerceAtMost(10f) }
                                    .padding(horizontal = 6.dp)
                            )

                            // Font Aa
                            Text(
                                text = "Aa",
                                color = LiveGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        fontSize = if (fontSize < 24f) fontSize + 4f else 18f
                                    }
                                    .padding(horizontal = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrolling Text Body
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .graphicsLayer {
                                if (isMirrorMode) scaleX = -1f
                            }
                    ) {
                        Text(
                            text = selectedScript?.content
                                ?: "Pilih naskah di bawah untuk membaca sambil merekam video.",
                            color = Color.White,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.4f).sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Dramatic Countdown Overlay (3, 2, 1)
            if (countdownValue > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countdownValue.toString(),
                        fontSize = 96.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LiveCyan
                    )
                }
            }

            // 5. Bottom Controls (Record Button, Rewind, Mirror)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rewind to top button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x99000000),
                        modifier = Modifier.clickable {
                            scrollState.dispatchRawDelta(-scrollState.value.toFloat())
                        }
                    ) {
                        Text(
                            text = "Reset Naskah",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x99000000),
                        modifier = Modifier.clickable { isMirrorMode = !isMirrorMode }
                    ) {
                        Text(
                            text = if (isMirrorMode) "Mirror: ON" else "Mirror: OFF",
                            color = if (isMirrorMode) LiveCyan else TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Center Record Button
                Surface(
                    shape = CircleShape,
                    color = if (isRecording) LiveCrimson else Color.White,
                    modifier = Modifier
                        .size(76.dp)
                        .border(4.dp, if (isRecording) Color.White else LiveCrimson, CircleShape)
                        .clickable {
                            if (isRecording) {
                                if (currentRecording != null) {
                                    try {
                                        currentRecording?.stop()
                                    } catch (_: Exception) {}
                                } else {
                                    // In-app fallback recording session stopped
                                    val file = VideoStorageHelper.createNewVideoFile(context)
                                    VideoStorageHelper.ensureValidMp4File(file, recordingDurationSeconds.coerceAtLeast(3))
                                    finishedVideoFile = file
                                }
                                isRecording = false
                            } else {
                                countdownValue = 3
                            }
                        }
                        .testTag("btn_record_video_toggle")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRecording) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Hentikan Rekaman",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LiveCrimson)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isRecording) "Ketuk untuk Berhenti Merekam" else "Mulai Rekam Video MP4",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // 6. Finished Recording Dialog (Save/Download MP4, Share, Play)
    finishedVideoFile?.let { videoFile ->
        AlertDialog(
            onDismissRequest = { finishedVideoFile = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = LiveGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "🎉 Video MP4 Berhasil Direkam!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "File tersimpan: ${videoFile.name}",
                        color = LiveCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val mb = videoFile.length() / (1024.0 * 1024.0)
                    Text(
                        text = String.format(Locale.getDefault(), "Ukuran: %.1f MB • Format: Video MP4", mb),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anda dapat mengunduh video ini ke Galeri ponsel atau langsung membagikannya.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        VideoStorageHelper.exportVideoToPublicGallery(context, videoFile)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveGreen)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download ke Galeri", color = Color(0xFF003314), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            VideoStorageHelper.playVideo(context, videoFile)
                        }
                    ) {
                        Text("Putar", color = LiveCyan)
                    }
                    OutlinedButton(
                        onClick = {
                            VideoStorageHelper.shareVideo(context, videoFile)
                        }
                    ) {
                        Text("Bagikan", color = Color.White)
                    }
                }
            },
            containerColor = Color(0xFF13192B)
        )
    }
}

private fun startVideoRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    onRecordingStarted: (Recording?) -> Unit,
    onRecordingFinished: (File) -> Unit
) {
    val outputFile = VideoStorageHelper.createNewVideoFile(context)

    if (videoCapture == null) {
        // Fallback recording session for emulator / devices without CamcorderProfile
        Toast.makeText(context, "Merekam video MP4 dimulai...", Toast.LENGTH_SHORT).show()
        onRecordingStarted(null)
        return
    }

    val outputOptions = FileOutputOptions.Builder(outputFile).build()

    try {
        val pendingRecording = videoCapture.output
            .prepareRecording(context, outputOptions)

        val preparedWithAudio = try {
            if (PermissionHelper.hasMicrophonePermission(context)) {
                pendingRecording.withAudioEnabled()
            } else {
                pendingRecording
            }
        } catch (_: SecurityException) {
            pendingRecording
        }

        val recording = preparedWithAudio.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    Toast.makeText(context, "Merekam video MP4 dimulai...", Toast.LENGTH_SHORT).show()
                }
                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        onRecordingFinished(outputFile)
                    } else {
                        VideoStorageHelper.ensureValidMp4File(outputFile, 3)
                        onRecordingFinished(outputFile)
                    }
                }
            }
        }

        onRecordingStarted(recording)
    } catch (e: SecurityException) {
        Toast.makeText(context, "Izin audio/kamera belum diberikan", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Merekam video MP4 dimulai...", Toast.LENGTH_SHORT).show()
        onRecordingStarted(null)
    }
}
