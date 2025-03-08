// PlaylistsViewModel.kt
package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class PlaylistsScreenState(
    val playlists: List<Playlist> = emptyList()
)

class PlaylistsViewModel(private val playlistInteractor: PlaylistInteractor) : ViewModel() {

    private val _state = MutableLiveData(PlaylistsScreenState())
    val state: LiveData<PlaylistsScreenState> = _state

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { list ->
                _state.postValue(PlaylistsScreenState(playlists = list))
            }
        }
    }
}
