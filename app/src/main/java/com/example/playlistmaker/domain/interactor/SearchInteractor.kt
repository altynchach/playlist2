package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun searchTracksFlow(query: String): Flow<List<Track>>

    fun getSearchHistory(): List<Track>
    fun saveTrackToHistory(track: Track)
    fun clearSearchHistory()
}
