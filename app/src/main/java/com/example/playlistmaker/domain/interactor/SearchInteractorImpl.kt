package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {
    override fun searchTracksFlow(query: String): Flow<List<Track>> {
        return trackRepository.searchTracksFlow(query)
    }

    override fun getSearchHistory(): List<Track> {
        return searchHistoryRepository.getHistory()
    }

    override fun saveTrackToHistory(track: Track) {
        searchHistoryRepository.saveTrack(track)
    }

    override fun clearSearchHistory() {
        searchHistoryRepository.clearHistory()
    }
}
