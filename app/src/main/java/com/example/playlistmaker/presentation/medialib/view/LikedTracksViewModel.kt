package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class LikedTracksScreenState(
    val tracks: List<Track> = emptyList(),
    val isEmpty: Boolean = true
)

class LikedTracksViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData<LikedTracksScreenState>()
    val state: LiveData<LikedTracksScreenState> = _state

    init {
        _state.value = LikedTracksScreenState()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesInteractor.getFavorites().collect { list ->
                _state.postValue(
                    LikedTracksScreenState(
                        tracks = list,
                        isEmpty = list.isEmpty()
                    )
                )
            }
        }
    }
}
