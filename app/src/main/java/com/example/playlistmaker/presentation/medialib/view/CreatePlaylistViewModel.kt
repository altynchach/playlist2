package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {


    suspend fun createPlaylist(name: String, description: String, coverPath: String?) {
        val playlist = Playlist(
            name = name,
            description = description,
            coverFilePath = coverPath,
            trackIds = emptyList(),
            trackCount = 0
        )
        playlistInteractor.createPlaylist(playlist)
    }


    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistInteractor.getPlaylistById(playlistId)
    }

    suspend fun updatePlaylist(
        playlistId: Long,
        name: String,
        description: String,
        newCoverPath: String?
    ) {
        val old = playlistInteractor.getPlaylistById(playlistId) ?: return
        val updated = old.copy(
            name = name,
            description = description,
            coverFilePath = newCoverPath
        )
        playlistInteractor.updatePlaylist(updated)
    }
}
