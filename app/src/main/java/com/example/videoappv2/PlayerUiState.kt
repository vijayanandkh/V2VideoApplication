package com.example.videoappv2

sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data object Play : PlayerUiState()
    data object Pause : PlayerUiState()
    data object Stop: PlayerUiState()
    data object Speed : PlayerUiState()
}