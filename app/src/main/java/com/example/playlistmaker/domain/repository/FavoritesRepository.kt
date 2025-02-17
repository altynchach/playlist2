package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavorites(): Flow<List<Track>>
    fun isFavorite(trackId: Long): Flow<Boolean>
    suspend fun addFavorite(track: Track)
    suspend fun removeFavorite(trackId: Long)
    fun getFavoriteIds(): Flow<List<Long>>
}
