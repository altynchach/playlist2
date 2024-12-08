package com.example.playlistmaker.presentation.search

import android.content.Context
import androidx.lifecycle.*
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.SearchScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor
) : ViewModel() {

    private val _state = MutableLiveData<SearchScreenState>(SearchScreenState())
    val state: LiveData<SearchScreenState> = _state

    private var searchJob: Job? = null
    private var currentQuery: String = ""

    fun init() {
        // Отобразим историю, если есть фокус и нет текста
        updateState(_state.value!!.copy(history = searchInteractor.getSearchHistory()))
    }

    fun onSearchQueryChanged(query: String) {
        currentQuery = query
        if (query.isEmpty()) {
            // Показываем историю
            updateState(_state.value!!.copy(
                showHistory = true,
                showResults = false,
                showNothingFound = false,
                showError = false,
                results = emptyList(),
                history = searchInteractor.getSearchHistory(),
                isLoading = false
            ))
        } else {
            updateState(_state.value!!.copy(
                showHistory = false,
                history = emptyList()
            ))
            debounceSearch(query)
        }
    }

    private fun debounceSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(2000)
            search(query)
        }
    }

    private fun search(query: String) {
        updateState(_state.value!!.copy(
            isLoading = true,
            showNothingFound = false,
            showError = false,
            showResults = false,
            results = emptyList()
        ))
        viewModelScope.launch {
            val result = searchInteractor.searchTracks(query)
            if (result.isSuccess) {
                val tracks = result.getOrThrow()
                if (tracks.isNotEmpty()) {
                    updateState(_state.value!!.copy(
                        isLoading = false,
                        showResults = true,
                        results = tracks,
                        showNothingFound = false,
                        showError = false
                    ))
                } else {
                    updateState(_state.value!!.copy(
                        isLoading = false,
                        showNothingFound = true,
                        showResults = false,
                        results = emptyList(),
                        showError = false
                    ))
                }
            } else {
                updateState(_state.value!!.copy(
                    isLoading = false,
                    showError = true,
                    showResults = false,
                    showNothingFound = false,
                    results = emptyList()
                ))
            }
        }
    }

    fun onFocusChanged(hasFocus: Boolean) {
        if (hasFocus && currentQuery.isEmpty()) {
            updateState(_state.value!!.copy(
                showHistory = true,
                history = searchInteractor.getSearchHistory()
            ))
        } else {
            updateState(_state.value!!.copy(showHistory = false))
        }
    }

    fun clearHistory() {
        searchInteractor.clearSearchHistory()
        if (currentQuery.isEmpty()) {
            updateState(_state.value!!.copy(
                showHistory = true,
                history = emptyList()
            ))
        }
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
        _state.value = newState
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val interactor = Creator.provideSearchInteractor(context)
            return SearchViewModel(interactor) as T
        }
    }
}
