package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistInteractor {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun updatePlaylist(playlist: Playlist)
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>
}
