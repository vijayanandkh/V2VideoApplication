package com.example.videoappv2

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoPlayerViewModel: ViewModel() {

    private val _playerUiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState

    private val _showPopupUiState = MutableStateFlow<String>("false")
    val showPopupUiState: StateFlow<String> = _showPopupUiState


    fun performCustomActions(action: String) {
        Log.d("ViewModel", " $action performed")
        when(action) {
            "play" -> _playerUiState.value = PlayerUiState.Play
            "pause" -> _playerUiState.value = PlayerUiState.Pause
            "stop" -> _playerUiState.value = PlayerUiState.Stop
            "speed" -> _playerUiState.value = PlayerUiState.Speed
            else -> Log.d("ViewModel", "Unknown action performed")
        }
    }

    fun updateShowPopupState(show: String) {
        Log.d("ViewModel", "Show popup State $show")
        _showPopupUiState.value = show
    }




}