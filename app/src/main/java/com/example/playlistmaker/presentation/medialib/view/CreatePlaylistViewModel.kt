package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val isCreateButtonEnabled: Boolean = false,
    val coverFilePath: String? = null,
    val isPlaylistCreated: Boolean = false,
    val createdPlaylistName: String = ""
)

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData(CreatePlaylistState())
    val state: LiveData<CreatePlaylistState> = _state

    private var coverPath: String? = null

    fun onNameChanged(newName: String) {
        updateState(isCreateButtonEnabled = newName.isNotBlank())
    }

    fun onCoverPicked(filePath: String?) {
        coverPath = filePath
        updateState(coverFilePath = filePath)
    }

    fun savePlaylist(name: String, description: String) {
        viewModelScope.launch {
            val playlist = Playlist(
                name = name.trim(),
                description = description.trim(),
                coverFilePath = coverPath,
                trackIds = emptyList(),
                trackCount = 0
            )
            playlistInteractor.createPlaylist(playlist)
            updateState(
                isPlaylistCreated = true,
                createdPlaylistName = name
            )
        }
    }

    fun loadPlaylistForEdit(playlistId: Long) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            if (playlist != null) {
                coverPath = playlist.coverFilePath
                updateState(
                    isCreateButtonEnabled = playlist.name.isNotBlank(),
                    coverFilePath = coverPath
                )
            }
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, description: String, coverPath: String?) {
        viewModelScope.launch {
            val playlist = playlistInteractor.getPlaylistById(playlistId)
            if (playlist != null) {
                val updated = playlist.copy(
                    name = name,
                    description = description,
                    coverFilePath = coverPath
                )
                playlistInteractor.updatePlaylist(updated)
            }
        }
    }

    fun hasCover(): Boolean = coverPath != null

    private fun updateState(
        isCreateButtonEnabled: Boolean? = null,
        coverFilePath: String? = null,
        isPlaylistCreated: Boolean? = null,
        createdPlaylistName: String? = null
    ) {
        val old = _state.value ?: CreatePlaylistState()
        val newState = old.copy(
            isCreateButtonEnabled = isCreateButtonEnabled ?: old.isCreateButtonEnabled,
            coverFilePath = coverFilePath ?: old.coverFilePath,
            isPlaylistCreated = isPlaylistCreated ?: old.isPlaylistCreated,
            createdPlaylistName = createdPlaylistName ?: old.createdPlaylistName
        )
        _state.value = newState
    }
}
