package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(playlist: Playlist): Long
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun getPlaylistById(playlistId: Long): Playlist?
    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Boolean
}
