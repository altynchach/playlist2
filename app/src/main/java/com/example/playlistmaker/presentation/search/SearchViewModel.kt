package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(private val searchInteractor: SearchInteractor) : ViewModel() {

    private val stateLiveData = MutableLiveData(SearchScreenState())
    fun getState(): LiveData<SearchScreenState> = stateLiveData

    private var currentQuery: String = ""

    private var jobSearch: Job? = null

    fun init() {
        val history = searchInteractor.getSearchHistory()
        updateState(
            stateLiveData.value!!.copy(
                history = history,
                showHistory = history.isNotEmpty()
            )
        )
    }

    fun onSearchQueryChanged(query: String) {
        currentQuery = query
        if (query.isEmpty()) {
            val history = searchInteractor.getSearchHistory()
            updateState(
                stateLiveData.value!!.copy(
                    showHistory = history.isNotEmpty(),
                    showResults = false,
                    showNothingFound = false,
                    showError = false,
                    results = emptyList(),
                    history = history,
                    isLoading = false
                )
            )
            return
        } else {
            updateState(stateLiveData.value!!.copy(showHistory = false, history = emptyList()))
        }

        jobSearch?.cancel()
        jobSearch = viewModelScope.launch {
            delay(2000)
            if (query.isNotEmpty()) {
                doSearch(query)
            }
        }
    }

    private fun doSearch(query: String) {
        updateState(
            stateLiveData.value!!.copy(
                isLoading = true,
                showNothingFound = false,
                showError = false,
                showResults = false,
                results = emptyList()
            )
        )
        viewModelScope.launch {
            searchInteractor.searchTracksFlow(query)
                .catch {
                    updateState(
                        stateLiveData.value!!.copy(
                            isLoading = false,
                            showError = true,
                            showResults = false,
                            showNothingFound = false,
                            results = emptyList()
                        )
                    )
                }
                .collect { tracks: List<Track> ->
                    if (tracks.isNotEmpty()) {
                        updateState(
                            stateLiveData.value!!.copy(
                                isLoading = false,
                                showResults = true,
                                results = tracks,
                                showNothingFound = false,
                                showError = false
                            )
                        )
                    } else {
                        updateState(
                            stateLiveData.value!!.copy(
                                isLoading = false,
                                showNothingFound = true,
                                showResults = false,
                                results = emptyList(),
                                showError = false
                            )
                        )
                    }
                }
        }
    }

    fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus && currentQuery.isEmpty()) {
            val history = searchInteractor.getSearchHistory()
            updateState(
                stateLiveData.value!!.copy(
                    showHistory = history.isNotEmpty(),
                    history = history
                )
            )
        } else {
            updateState(stateLiveData.value!!.copy(showHistory = false))
        }
    }

    fun clearHistory() {
        searchInteractor.clearSearchHistory()
        val history = searchInteractor.getSearchHistory()
        updateState(
            stateLiveData.value!!.copy(
                showHistory = currentQuery.isEmpty() && history.isNotEmpty(),
                history = history
            )
        )
    }

    fun saveTrackToHistory(track: Track) {
        searchInteractor.saveTrackToHistory(track)
    }

    fun onReloadClicked() {
        if (currentQuery.isNotEmpty()) {
            doSearch(currentQuery)
        }
    }

    private fun updateState(newState: SearchScreenState) {
        stateLiveData.value = newState
    }
}
