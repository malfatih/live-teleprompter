package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.LiveTeleprompterApplication
import com.example.MainActivity
import com.example.R
import com.example.data.preferences.PrompterPreferencesRepository
import com.example.data.preferences.PrompterSettings
import com.example.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class FloatingTeleprompterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null

    // Window Views
    private var windowView: View? = null
    private var bubbleView: View? = null
    private var windowLayoutParams: WindowManager.LayoutParams? = null
    private var bubbleLayoutParams: WindowManager.LayoutParams? = null

    // UI Components
    private var titleTextView: TextView? = null
    private var scriptTextView: TextView? = null
    private var scrollView: ScrollView? = null
    private var playPauseButton: ImageButton? = null
    private var speedTextView: TextView? = null
    private var controlBarLayout: LinearLayout? = null
    private var mainCardLayout: LinearLayout? = null

    // State
    private var currentScriptTitle: String = "Naskah Live"
    private var currentScriptContent: String = ""
    private var currentScriptId: Long = 0L

    private var isPlaying: Boolean = false
    private var isMinimized: Boolean = false
    private var controlsVisible: Boolean = true

    private var currentSpeed: Float = 3.0f
    private var currentFontSizeSp: Float = 20.0f
    private var currentTextColor: Int = Color.WHITE
    private var currentBgOpacity: Float = 0.75f
    private var isMirrorMode: Boolean = false
    private var keepScreenOn: Boolean = true
    private var voiceFollowingEnabled: Boolean = false

    // Auto-scroll loop
    private val scrollHandler = Handler(Looper.getMainLooper())
    private val scrollRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && scrollView != null && scriptTextView != null) {
                val step = (currentSpeed * 0.85f).coerceAtLeast(1f).toInt()
                scrollView?.scrollBy(0, step)
                scrollHandler.postDelayed(this, 32L) // ~30 fps
            }
        }
    }

    // Optional Speech Recognizer
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        _isServiceRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PLAY_PAUSE -> {
                togglePlayPause()
                updateNotification()
                return START_STICKY
            }
            ACTION_REWIND -> {
                rewind(200)
                return START_STICKY
            }
            ACTION_FORWARD -> {
                forward(200)
                return START_STICKY
            }
        }

        // Normal start with script
        val scriptId = intent?.getLongExtra(EXTRA_SCRIPT_ID, 0L) ?: 0L
        val scriptTitle = intent?.getStringExtra(EXTRA_SCRIPT_TITLE) ?: "Naskah Live"
        val scriptContent = intent?.getStringExtra(EXTRA_SCRIPT_CONTENT) ?: ""

        if (scriptContent.isNotBlank()) {
            currentScriptId = scriptId
            currentScriptTitle = scriptTitle
            currentScriptContent = scriptContent
            _activeScriptTitle.value = scriptTitle
        }

        startForegroundServiceWithNotification()

        serviceScope.launch {
            val app = application as? LiveTeleprompterApplication
            val prefs = app?.preferencesRepository?.settingsFlow?.first() ?: PrompterSettings()
            currentSpeed = prefs.scrollSpeed
            currentFontSizeSp = prefs.fontSizeSp
            currentTextColor = try { Color.parseColor(prefs.textColorHex) } catch (_: Exception) { Color.WHITE }
            currentBgOpacity = prefs.backgroundOpacity
            isMirrorMode = prefs.isMirrorMode
            keepScreenOn = prefs.keepScreenOn
            voiceFollowingEnabled = prefs.voiceFollowingEnabled

            showFloatingWindow()

            if (voiceFollowingEnabled && PermissionHelper.hasMicrophonePermission(this@FloatingTeleprompterService)) {
                setupVoiceFollowing()
            }
        }

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                LiveTeleprompterApplication.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(LiveTeleprompterApplication.NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseIntent = Intent(this, FloatingTeleprompterService::class.java).apply {
            action = ACTION_PLAY_PAUSE
        }
        val playPausePendingIntent = PendingIntent.getService(
            this,
            1,
            playPauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FloatingTeleprompterService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseActionTitle = if (isPlaying) "Jeda" else "Lanjutkan"

        return NotificationCompat.Builder(this, LiveTeleprompterApplication.CHANNEL_ID)
            .setContentTitle("Live Teleprompter: $currentScriptTitle")
            .setContentText(if (isPlaying) "Teks sedang berjalan (Kecepatan ${currentSpeed.toInt()}x)" else "Teleprompter dijeda")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, playPauseActionTitle, playPausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tutup", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(LiveTeleprompterApplication.NOTIFICATION_ID, notification)
    }

    private fun showFloatingWindow() {
        if (!PermissionHelper.canDrawOverlays(this)) {
            Toast.makeText(this, "Izin Tampilkan di Atas Aplikasi Lain diperlukan", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        if (windowView != null) {
            updateFloatingViewContent()
            return
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (keepScreenOn) {
            flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }

        val displayMetrics = resources.displayMetrics
        val initialWidth = (displayMetrics.widthPixels * 0.88f).toInt().coerceIn(dpToPx(280), dpToPx(480))
        val initialHeight = dpToPx(280)

        windowLayoutParams = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            overlayType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (displayMetrics.widthPixels - initialWidth) / 2
            y = dpToPx(70)
        }

        windowView = buildPrompterLayout()
        try {
            windowManager?.addView(windowView, windowLayoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Izin Tampilkan di Atas Aplikasi Lain diperlukan", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        // Build Minimized Bubble
        buildMinimizedBubble()
    }

    private fun buildPrompterLayout(): View {
        val root = FrameLayout(this)

        // Main Card
        mainCardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val bg = GradientDrawable().apply {
                setColor(adjustAlpha(Color.parseColor("#0C0F1E"), currentBgOpacity))
                cornerRadius = dpToPx(16).toFloat()
                setStroke(dpToPx(1), Color.parseColor("#384260"))
            }
            background = bg
            setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8))
        }

        // 1. Header Bar (Drag handle, Title, Minimize, Close)
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(6))
        }

        val dragIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_sort_by_size)
            setColorFilter(Color.parseColor("#9AA4BF"))
            layoutParams = LinearLayout.LayoutParams(dpToPx(20), dpToPx(20)).apply {
                marginEnd = dpToPx(6)
            }
        }

        titleTextView = TextView(this).apply {
            text = currentScriptTitle
            setTextColor(Color.parseColor("#00E5FF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            isSingleLine = true
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
        }

        // Minimize button
        val minBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
            setOnClickListener { minimizePrompter() }
        }

        // Close button
        val closeBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.parseColor("#FF5252"))
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
            setOnClickListener { stopSelf() }
        }

        header.addView(dragIcon)
        header.addView(titleTextView)
        header.addView(minBtn)
        header.addView(closeBtn)

        // Drag listener for whole header
        header.setOnTouchListener(createDragTouchListener())

        // 2. Reading Text Area (ScrollView)
        scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            isVerticalScrollBarEnabled = true
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        scriptTextView = TextView(this).apply {
            text = if (currentScriptContent.isNotBlank()) currentScriptContent else "Naskah kosong."
            setTextColor(currentTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp)
            setLineSpacing(0f, 1.35f)
            typeface = Typeface.DEFAULT
            setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(80))
            if (isMirrorMode) {
                scaleX = -1f
            }
        }
        scrollView?.addView(scriptTextView)

        // 3. Floating Control Bar (Play/Pause, Speed -/+, Rewind, Font -/+, Toggle hide)
        controlBarLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val controlBg = GradientDrawable().apply {
                setColor(Color.parseColor("#E613182C"))
                cornerRadius = dpToPx(12).toFloat()
            }
            background = controlBg
            setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(6)
            }
        }

        // Rewind button
        val rewindBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_rew)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dpToPx(36), dpToPx(36))
            setOnClickListener { rewind(200) }
        }

        // Play/Pause button
        playPauseButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_play)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.parseColor("#00E5FF"))
            layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))
            setOnClickListener { togglePlayPause() }
        }

        // Forward button
        val forwardBtn = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_media_ff)
            setBackgroundColor(Color.TRANSPARENT)
            setColorFilter(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dpToPx(36), dpToPx(36))
            setOnClickListener { forward(200) }
        }

        // Speed Down (-)
        val speedDownBtn = TextView(this).apply {
            text = "−"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(28), dpToPx(36))
            setOnClickListener {
                currentSpeed = (currentSpeed - 0.5f).coerceAtLeast(1.0f)
                updateSpeedDisplay()
            }
        }

        // Speed display
        speedTextView = TextView(this).apply {
            text = "${currentSpeed.toInt()}x"
            setTextColor(Color.parseColor("#FFD54F"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), dpToPx(36))
        }

        // Speed Up (+)
        val speedUpBtn = TextView(this).apply {
            text = "+"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(28), dpToPx(36))
            setOnClickListener {
                currentSpeed = (currentSpeed + 0.5f).coerceAtMost(10.0f)
                updateSpeedDisplay()
            }
        }

        // Font Size toggle
        val fontToggleBtn = TextView(this).apply {
            text = "Aa"
            setTextColor(Color.parseColor("#7C4DFF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(36))
            setOnClickListener {
                currentFontSizeSp = when {
                    currentFontSizeSp < 18f -> 22f
                    currentFontSizeSp < 24f -> 28f
                    currentFontSizeSp < 32f -> 36f
                    else -> 16f
                }
                scriptTextView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp)
            }
        }

        // Mirror mode quick toggle
        val mirrorToggleBtn = TextView(this).apply {
            text = "🪞"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(30), dpToPx(36))
            setOnClickListener {
                isMirrorMode = !isMirrorMode
                scriptTextView?.scaleX = if (isMirrorMode) -1f else 1f
                Toast.makeText(this@FloatingTeleprompterService, if (isMirrorMode) "Mirror Mode Aktif" else "Normal Mode", Toast.LENGTH_SHORT).show()
            }
        }

        // Top button (reset to start)
        val toTopBtn = TextView(this).apply {
            text = "Top"
            setTextColor(Color.parseColor("#9AA4BF"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(36))
            setOnClickListener {
                scrollView?.smoothScrollTo(0, 0)
            }
        }

        controlBarLayout?.addView(rewindBtn)
        controlBarLayout?.addView(playPauseButton)
        controlBarLayout?.addView(forwardBtn)
        controlBarLayout?.addView(speedDownBtn)
        controlBarLayout?.addView(speedTextView)
        controlBarLayout?.addView(speedUpBtn)
        controlBarLayout?.addView(fontToggleBtn)
        controlBarLayout?.addView(mirrorToggleBtn)
        controlBarLayout?.addView(toTopBtn)

        mainCardLayout?.addView(header)
        mainCardLayout?.addView(scrollView)
        mainCardLayout?.addView(controlBarLayout)

        // 4. Resize handle at bottom right corner
        val resizeGrip = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_menu_crop)
            setColorFilter(Color.parseColor("#7C4DFF"))
            layoutParams = FrameLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                marginEnd = dpToPx(2)
                bottomMargin = dpToPx(2)
            }
            setOnTouchListener(createResizeTouchListener())
        }

        root.addView(mainCardLayout, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        root.addView(resizeGrip)

        return root
    }

    private fun buildMinimizedBubble() {
        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleLayoutParams = WindowManager.LayoutParams(
            dpToPx(56),
            dpToPx(56),
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dpToPx(16)
            y = dpToPx(150)
        }

        bubbleView = FrameLayout(this).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#FF3366"))
                setStroke(dpToPx(2), Color.WHITE)
            }
            background = bg

            val icon = ImageView(this@FloatingTeleprompterService).apply {
                setImageResource(android.R.drawable.ic_media_play)
                setColorFilter(Color.WHITE)
                layoutParams = FrameLayout.LayoutParams(dpToPx(28), dpToPx(28)).apply {
                    gravity = Gravity.CENTER
                }
            }
            addView(icon)

            setOnTouchListener(createBubbleTouchListener())
        }
    }

    private fun updateFloatingViewContent() {
        titleTextView?.text = currentScriptTitle
        scriptTextView?.text = currentScriptContent
        scriptTextView?.setTextColor(currentTextColor)
        scriptTextView?.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentFontSizeSp)
        scriptTextView?.scaleX = if (isMirrorMode) -1f else 1f
        val bg = GradientDrawable().apply {
            setColor(adjustAlpha(Color.parseColor("#0C0F1E"), currentBgOpacity))
            cornerRadius = dpToPx(16).toFloat()
            setStroke(dpToPx(1), Color.parseColor("#384260"))
        }
        mainCardLayout?.background = bg
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        _isPrompterPlaying.value = isPlaying
        if (isPlaying) {
            playPauseButton?.setImageResource(android.R.drawable.ic_media_pause)
            scrollHandler.post(scrollRunnable)
        } else {
            playPauseButton?.setImageResource(android.R.drawable.ic_media_play)
            scrollHandler.removeCallbacks(scrollRunnable)
        }
        updateNotification()
    }

    private fun rewind(pixels: Int) {
        scrollView?.let {
            val current = it.scrollY
            it.smoothScrollTo(0, (current - pixels).coerceAtLeast(0))
        }
    }

    private fun forward(pixels: Int) {
        scrollView?.let {
            val current = it.scrollY
            it.smoothScrollTo(0, current + pixels)
        }
    }

    private fun updateSpeedDisplay() {
        speedTextView?.text = "${currentSpeed.toInt()}x"
    }

    private fun minimizePrompter() {
        isMinimized = true
        if (windowView != null) {
            windowManager?.removeView(windowView)
        }
        if (bubbleView != null && bubbleLayoutParams != null) {
            windowManager?.addView(bubbleView, bubbleLayoutParams)
        }
    }

    private fun restorePrompter() {
        isMinimized = false
        if (bubbleView != null) {
            try {
                windowManager?.removeView(bubbleView)
            } catch (_: Exception) {}
        }
        if (windowView != null && windowLayoutParams != null) {
            windowManager?.addView(windowView, windowLayoutParams)
        }
    }

    // Touch listener for dragging the main window
    private fun createDragTouchListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = windowLayoutParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(windowView, params)
                        return true
                    }
                }
                return false
            }
        }
    }

    // Touch listener for resizing the window via bottom-right grip
    private fun createResizeTouchListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialWidth = 0
            private var initialHeight = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = windowLayoutParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialWidth = params.width
                        initialHeight = params.height
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newWidth = (initialWidth + (event.rawX - initialTouchX).toInt()).coerceIn(dpToPx(240), dpToPx(550))
                        val newHeight = (initialHeight + (event.rawY - initialTouchY).toInt()).coerceIn(dpToPx(160), dpToPx(700))
                        params.width = newWidth
                        params.height = newHeight
                        windowManager?.updateViewLayout(windowView, params)
                        return true
                    }
                }
                return false
            }
        }
    }

    // Touch listener for dragging or tapping the minimized bubble
    private fun createBubbleTouchListener(): View.OnTouchListener {
        return object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = bubbleLayoutParams ?: return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(bubbleView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            restorePrompter()
                        }
                        return true
                    }
                }
                return false
            }
        }
    }

    // Voice Following Setup (SpeechRecognizer)
    private fun setupVoiceFollowing() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) return

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        // Restart listener if still enabled
                        if (voiceFollowingEnabled && isPlaying) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                startVoiceListening()
                            }, 1000L)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0].lowercase(Locale.getDefault())
                            handleRecognizedSpeech(recognized)
                        }
                        if (voiceFollowingEnabled && isPlaying) {
                            startVoiceListening()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0].lowercase(Locale.getDefault())
                            handleRecognizedSpeech(recognized)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            startVoiceListening()
        } catch (_: Exception) {
            // Graceful fallback if mic is blocked or in use by camera
        }
    }

    private fun startVoiceListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    private fun handleRecognizedSpeech(speech: String) {
        // If word is detected, smooth scroll down slightly
        if (speech.isNotBlank()) {
            forward(40)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollHandler.removeCallbacks(scrollRunnable)
        serviceScope.cancel()

        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}

        if (windowView != null) {
            try { windowManager?.removeView(windowView) } catch (_: Exception) {}
        }
        if (bubbleView != null) {
            try { windowManager?.removeView(bubbleView) } catch (_: Exception) {}
        }

        _isServiceRunning.value = false
        _isPrompterPlaying.value = false
        _activeScriptTitle.value = ""
    }

    companion object {
        const val ACTION_STOP = "com.example.ACTION_STOP"
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_REWIND = "com.example.ACTION_REWIND"
        const val ACTION_FORWARD = "com.example.ACTION_FORWARD"

        const val EXTRA_SCRIPT_ID = "extra_script_id"
        const val EXTRA_SCRIPT_TITLE = "extra_script_title"
        const val EXTRA_SCRIPT_CONTENT = "extra_script_content"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _isPrompterPlaying = MutableStateFlow(false)
        val isPrompterPlaying: StateFlow<Boolean> = _isPrompterPlaying.asStateFlow()

        private val _activeScriptTitle = MutableStateFlow("")
        val activeScriptTitle: StateFlow<String> = _activeScriptTitle.asStateFlow()

        fun start(context: Context, id: Long, title: String, content: String) {
            val intent = Intent(context, FloatingTeleprompterService::class.java).apply {
                putExtra(EXTRA_SCRIPT_ID, id)
                putExtra(EXTRA_SCRIPT_TITLE, title)
                putExtra(EXTRA_SCRIPT_CONTENT, content)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingTeleprompterService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
