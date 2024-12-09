package com.example.playlistmaker.presentation.states

import com.example.playlistmaker.domain.models.Track

data class PlayerScreenState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val currentTimeFormatted: String = "00:00"
)
