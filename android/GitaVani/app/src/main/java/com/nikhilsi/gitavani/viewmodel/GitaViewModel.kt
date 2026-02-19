package com.nikhilsi.gitavani.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikhilsi.gitavani.audio.AudioService
import com.nikhilsi.gitavani.data.GitaDataService
import com.nikhilsi.gitavani.state.AppSettings
import com.nikhilsi.gitavani.state.ReadingProgress
import com.nikhilsi.gitavani.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GitaViewModel(application: Application) : AndroidViewModel(application) {
    val dataService = GitaDataService()
    val settings = AppSettings(application)
    val readingProgress = ReadingProgress(application)
    val audioService = AudioService(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    val currentTheme: StateFlow<AppTheme> = settings.selectedTheme
        .map { AppTheme.named(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.Sattva)

    init {
        viewModelScope.launch {
            dataService.loadData(application)
            if (dataService.isLoaded) {
                _isLoading.value = false
            } else {
                _loadError.value = dataService.loadError
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioService.stop()
    }
}
