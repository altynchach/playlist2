package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track

interface SearchInteractor {
    suspend fun searchTracks(query: String): Result<List<Track>>
    fun getSearchHistory(): List<Track>
    fun saveTrackToHistory(track: Track)
    fun clearSearchHistory()
}
