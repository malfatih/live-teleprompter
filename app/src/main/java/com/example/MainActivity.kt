package com.example

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ScriptEntity
import com.example.ui.MainViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.editor.ScriptEditorScreen
import com.example.ui.fullscreen.FullScreenPrompterScreen
import com.example.ui.record.RecordedVideosListScreen
import com.example.ui.record.VideoRecordScreen
import com.example.ui.scripts.ScriptListScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splitscreen.SplitScreenGuideScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PermissionHelper

sealed class Screen {
    data object Dashboard : Screen()
    data object ScriptList : Screen()
    data class ScriptEditor(val scriptId: Long) : Screen()
    data class FullScreen(val scriptId: Long) : Screen()
    data class VideoRecord(val scriptId: Long) : Screen()
    data object RecordedVideosList : Screen()
    data object SplitScreen : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as LiveTeleprompterApplication
        val viewModelFactory = MainViewModel.Factory(
            app.scriptRepository,
            app.preferencesRepository
        )

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

                // Request Notification Permission on Android 13+
                val notificationLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    // Handled
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (!PermissionHelper.hasNotificationPermission(this@MainActivity)) {
                            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B0E19)
                ) {
                    when (val screen = currentScreen) {
                        is Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { scriptId ->
                                    currentScreen = Screen.ScriptEditor(scriptId)
                                },
                                onNavigateToList = {
                                    currentScreen = Screen.ScriptList
                                },
                                onNavigateToFullScreen = { scriptId ->
                                    currentScreen = Screen.FullScreen(scriptId)
                                },
                                onNavigateToRecord = { scriptId ->
                                    currentScreen = Screen.VideoRecord(scriptId)
                                },
                                onNavigateToGallery = {
                                    currentScreen = Screen.RecordedVideosList
                                },
                                onNavigateToSplitScreen = {
                                    currentScreen = Screen.SplitScreen
                                },
                                onNavigateToSettings = {
                                    currentScreen = Screen.Settings
                                }
                            )
                        }

                        is Screen.ScriptList -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            ScriptListScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onNavigateToEditor = { scriptId ->
                                    currentScreen = Screen.ScriptEditor(scriptId)
                                },
                                onNavigateToFullScreen = { scriptId ->
                                    currentScreen = Screen.FullScreen(scriptId)
                                }
                            )
                        }

                        is Screen.ScriptEditor -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            ScriptEditorScreen(
                                scriptId = screen.scriptId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onLaunchFloatingDirectly = { script ->
                                    if (PermissionHelper.canDrawOverlays(this@MainActivity)) {
                                        viewModel.startFloating(this@MainActivity, script)
                                        Toast.makeText(this@MainActivity, "Floating Teleprompter Berjalan!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val intent = PermissionHelper.getOverlayPermissionIntent(this@MainActivity)
                                        startActivity(intent)
                                    }
                                }
                            )
                        }

                        is Screen.FullScreen -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            FullScreenPrompterScreen(
                                scriptId = screen.scriptId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard }
                            )
                        }

                        is Screen.VideoRecord -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            VideoRecordScreen(
                                initialScriptId = screen.scriptId,
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onNavigateToGallery = { currentScreen = Screen.RecordedVideosList }
                            )
                        }

                        is Screen.RecordedVideosList -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            RecordedVideosListScreen(
                                onNavigateBack = { currentScreen = Screen.Dashboard },
                                onNavigateToRecord = {
                                    val firstScriptId = viewModel.allScripts.value.firstOrNull()?.id ?: 0L
                                    currentScreen = Screen.VideoRecord(firstScriptId)
                                }
                            )
                        }

                        is Screen.SplitScreen -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            SplitScreenGuideScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard }
                            )
                        }

                        is Screen.Settings -> {
                            BackHandler { currentScreen = Screen.Dashboard }
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = Screen.Dashboard }
                            )
                        }
                    }
                }
            }
        }
    }
}
