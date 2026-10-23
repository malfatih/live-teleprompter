package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScriptEntity
import com.example.data.preferences.PrompterPreferencesRepository
import com.example.data.preferences.PrompterSettings
import com.example.data.repository.ScriptRepository
import com.example.service.FloatingTeleprompterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val scriptRepository: ScriptRepository,
    private val preferencesRepository: PrompterPreferencesRepository
) : ViewModel() {

    val allScripts: StateFlow<List<ScriptEntity>> = scriptRepository.allScripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<PrompterSettings> = preferencesRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrompterSettings())

    val isFloatingServiceRunning: StateFlow<Boolean> = FloatingTeleprompterService.isServiceRunning
    val isPrompterPlaying: StateFlow<Boolean> = FloatingTeleprompterService.isPrompterPlaying
    val activeFloatingTitle: StateFlow<String> = FloatingTeleprompterService.activeScriptTitle

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun saveScript(
        id: Long = 0,
        title: String,
        content: String,
        category: String = "Live Jualan",
        isFavorite: Boolean = false,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val script = ScriptEntity(
                id = id,
                title = title.ifBlank { "Naskah Tanpa Judul" },
                content = content,
                category = category,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            val newId = if (id == 0L) {
                scriptRepository.insert(script)
            } else {
                scriptRepository.update(script)
                id
            }
            onSaved(newId)
        }
    }

    fun duplicateScript(script: ScriptEntity) {
        viewModelScope.launch {
            val duplicated = script.copy(
                id = 0,
                title = "${script.title} (Salinan)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            scriptRepository.insert(duplicated)
        }
    }

    fun deleteScript(id: Long) {
        viewModelScope.launch {
            scriptRepository.deleteById(id)
        }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            scriptRepository.toggleFavorite(id, isFavorite)
        }
    }

    fun startFloating(context: Context, script: ScriptEntity) {
        viewModelScope.launch {
            scriptRepository.updateLastUsed(script.id)
            preferencesRepository.updateLastActiveScriptId(script.id)
            FloatingTeleprompterService.start(
                context = context,
                id = script.id,
                title = script.title,
                content = script.content
            )
        }
    }

    fun stopFloating(context: Context) {
        FloatingTeleprompterService.stop(context)
    }

    fun updateScrollSpeed(speed: Float) {
        viewModelScope.launch { preferencesRepository.updateScrollSpeed(speed) }
    }

    fun updateFontSize(sizeSp: Float) {
        viewModelScope.launch { preferencesRepository.updateFontSize(sizeSp) }
    }

    fun updateTextColor(hex: String) {
        viewModelScope.launch { preferencesRepository.updateTextColor(hex) }
    }

    fun updateBackgroundOpacity(opacity: Float) {
        viewModelScope.launch { preferencesRepository.updateBackgroundOpacity(opacity) }
    }

    fun updateLineSpacing(spacing: Float) {
        viewModelScope.launch { preferencesRepository.updateLineSpacing(spacing) }
    }

    fun updateMirrorMode(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateMirrorMode(enabled) }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateKeepScreenOn(enabled) }
    }

    fun updateVoiceFollowing(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.updateVoiceFollowing(enabled) }
    }

    fun resetSettings() {
        viewModelScope.launch { preferencesRepository.resetDefaults() }
    }

    class Factory(
        private val scriptRepository: ScriptRepository,
        private val preferencesRepository: PrompterPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(scriptRepository, preferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
