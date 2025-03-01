package com.example.playlistmaker.presentation.trackinfo

import com.example.playlistmaker.domain.models.Track

data class TrackInfoScreenState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val currentTimeFormatted: String = "00:00",
    val isFavorite: Boolean = false,
    // Например, общее время трека
    val durationFormatted: String = "00:00"
)
