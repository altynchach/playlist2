package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): Boolean
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun deletePlaylist(playlistId: Long): Unit
    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>>
}
