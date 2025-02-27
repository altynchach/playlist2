package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(private val repository: PlaylistRepository) : PlaylistInteractor {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return repository.createPlaylist(playlist)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Boolean {
        return repository.addTrackToPlaylist(playlistId, trackId)
    }
}
