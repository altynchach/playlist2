package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track

interface SearchInteractor {
    fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onFailure: () -> Unit
    )
    fun getSearchHistory(): List<Track>
    fun saveTrackToHistory(track: Track)
    fun clearSearchHistory()
}
