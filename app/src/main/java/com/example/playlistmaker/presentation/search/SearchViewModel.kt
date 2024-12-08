package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.SearchScreenState

class SearchViewModel(private val searchInteractor: SearchInteractor) : ViewModel() {

    private val stateLiveData = MutableLiveData(SearchScreenState())
    fun getState(): LiveData<SearchScreenState> = stateLiveData

    private var currentQuery: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

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
        } else {
            updateState(
                stateLiveData.value!!.copy(
                    showHistory = false,
                    history = emptyList()
                )
            )
            debounceSearch(query)
        }
    }

    private fun debounceSearch(query: String) {
        searchRunnable?.let { handler.removeCallbacks(it) }
        searchRunnable = Runnable {
            search(query)
        }
        handler.postDelayed(searchRunnable!!, 2000)
    }

    private fun search(query: String) {
        updateState(
            stateLiveData.value!!.copy(
                isLoading = true,
                showNothingFound = false,
                showError = false,
                showResults = false,
                results = emptyList()
            )
        )
        searchInteractor.searchTracks(query,
            { tracks: List<Track> ->
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
            },
            {
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
        )
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
        // Если строка поиска пустая и история пустая — скрываем историю
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
            search(currentQuery)
        }
    }

    private fun updateState(newState: SearchScreenState) {
        stateLiveData.value = newState
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val interactor = Creator.provideSearchInteractor(context)
            return SearchViewModel(interactor) as T
        }
    }
}
