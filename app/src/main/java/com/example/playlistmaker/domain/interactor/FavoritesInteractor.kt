package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface FavoritesInteractor {
    fun getFavorites(): Flow<List<Track>>
    fun isFavorite(trackId: Long): Flow<Boolean>
    suspend fun addFavorite(track: Track)
    suspend fun removeFavorite(trackId: Long)
    fun getFavoriteIds(): Flow<List<Long>>
}
