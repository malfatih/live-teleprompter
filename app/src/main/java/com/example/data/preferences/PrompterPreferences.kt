package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prompter_settings")

data class PrompterSettings(
    val scrollSpeed: Float = 3.0f,
    val fontSizeSp: Float = 20.0f,
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#0A0D18",
    val backgroundOpacity: Float = 0.75f,
    val lineSpacingMultiplier: Float = 1.35f,
    val isMirrorMode: Boolean = false,
    val keepScreenOn: Boolean = true,
    val voiceFollowingEnabled: Boolean = false,
    val windowWidthDp: Int = 320,
    val windowHeightDp: Int = 260,
    val lastActiveScriptId: Long = 0L
)

class PrompterPreferencesRepository(private val context: Context) {

    private object Keys {
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val TEXT_COLOR_HEX = stringPreferencesKey("text_color_hex")
        val BG_COLOR_HEX = stringPreferencesKey("bg_color_hex")
        val BG_OPACITY = floatPreferencesKey("bg_opacity")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val IS_MIRROR_MODE = booleanPreferencesKey("is_mirror_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val VOICE_FOLLOWING = booleanPreferencesKey("voice_following")
        val WINDOW_WIDTH = intPreferencesKey("window_width")
        val WINDOW_HEIGHT = intPreferencesKey("window_height")
        val LAST_ACTIVE_SCRIPT_ID = longPreferencesKey("last_active_script_id")
    }

    val settingsFlow: Flow<PrompterSettings> = context.dataStore.data.map { preferences ->
        PrompterSettings(
            scrollSpeed = preferences[Keys.SCROLL_SPEED] ?: 3.0f,
            fontSizeSp = preferences[Keys.FONT_SIZE_SP] ?: 20.0f,
            textColorHex = preferences[Keys.TEXT_COLOR_HEX] ?: "#FFFFFF",
            backgroundColorHex = preferences[Keys.BG_COLOR_HEX] ?: "#0A0D18",
            backgroundOpacity = preferences[Keys.BG_OPACITY] ?: 0.75f,
            lineSpacingMultiplier = preferences[Keys.LINE_SPACING] ?: 1.35f,
            isMirrorMode = preferences[Keys.IS_MIRROR_MODE] ?: false,
            keepScreenOn = preferences[Keys.KEEP_SCREEN_ON] ?: true,
            voiceFollowingEnabled = preferences[Keys.VOICE_FOLLOWING] ?: false,
            windowWidthDp = preferences[Keys.WINDOW_WIDTH] ?: 320,
            windowHeightDp = preferences[Keys.WINDOW_HEIGHT] ?: 260,
            lastActiveScriptId = preferences[Keys.LAST_ACTIVE_SCRIPT_ID] ?: 0L
        )
    }

    suspend fun updateScrollSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.SCROLL_SPEED] = speed.coerceIn(1.0f, 10.0f) }
    }

    suspend fun updateFontSize(sizeSp: Float) {
        context.dataStore.edit { it[Keys.FONT_SIZE_SP] = sizeSp.coerceIn(14.0f, 38.0f) }
    }

    suspend fun updateTextColor(hex: String) {
        context.dataStore.edit { it[Keys.TEXT_COLOR_HEX] = hex }
    }

    suspend fun updateBackgroundOpacity(opacity: Float) {
        context.dataStore.edit { it[Keys.BG_OPACITY] = opacity.coerceIn(0.2f, 0.95f) }
    }

    suspend fun updateLineSpacing(spacing: Float) {
        context.dataStore.edit { it[Keys.LINE_SPACING] = spacing }
    }

    suspend fun updateMirrorMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_MIRROR_MODE] = enabled }
    }

    suspend fun updateKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun updateVoiceFollowing(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_FOLLOWING] = enabled }
    }

    suspend fun updateWindowDimensions(widthDp: Int, heightDp: Int) {
        context.dataStore.edit {
            it[Keys.WINDOW_WIDTH] = widthDp.coerceIn(200, 600)
            it[Keys.WINDOW_HEIGHT] = heightDp.coerceIn(160, 800)
        }
    }

    suspend fun updateLastActiveScriptId(id: Long) {
        context.dataStore.edit { it[Keys.LAST_ACTIVE_SCRIPT_ID] = id }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit {
            it[Keys.SCROLL_SPEED] = 3.0f
            it[Keys.FONT_SIZE_SP] = 20.0f
            it[Keys.TEXT_COLOR_HEX] = "#FFFFFF"
            it[Keys.BG_COLOR_HEX] = "#0A0D18"
            it[Keys.BG_OPACITY] = 0.75f
            it[Keys.LINE_SPACING] = 1.35f
            it[Keys.IS_MIRROR_MODE] = false
            it[Keys.KEEP_SCREEN_ON] = true
            it[Keys.VOICE_FOLLOWING] = false
            it[Keys.WINDOW_WIDTH] = 320
            it[Keys.WINDOW_HEIGHT] = 260
        }
    }
}
