package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track

interface TrackRepository {
    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onFailure: () -> Unit
    )
}
