package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

class PlaylistInteractorImpl(private val repository: PlaylistRepository) : PlaylistInteractor {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        return repository.createPlaylist(playlist)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean {
        return repository.addTrackToPlaylist(playlistId, track)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        repository.deletePlaylist(playlistId)
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean {
        return repository.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return repository.getPlaylistById(playlistId)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return repository.getTracksForPlaylist(playlistId)
    }
}
