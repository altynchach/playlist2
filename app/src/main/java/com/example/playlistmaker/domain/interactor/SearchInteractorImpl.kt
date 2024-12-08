package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.TrackRepository

class SearchInteractorImpl(
    private val trackRepository: TrackRepository,
    private val searchHistoryRepository: SearchHistoryRepository
) : SearchInteractor {

    override fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onFailure: () -> Unit
    ) {
        trackRepository.searchTracks(query, onSuccess, onFailure)
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
