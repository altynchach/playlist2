package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val favoritesRepository: FavoritesRepository
) : FavoritesInteractor {

    override fun getFavorites(): Flow<List<Track>> {
        return favoritesRepository.getFavorites()
    }

    override fun isFavorite(trackId: Long): Flow<Boolean> {
        return favoritesRepository.isFavorite(trackId)
    }

    override suspend fun addFavorite(track: Track) {
        favoritesRepository.addFavorite(track)
    }

    override suspend fun removeFavorite(trackId: Long) {
        favoritesRepository.removeFavorite(trackId)
    }

    override fun getFavoriteIds(): Flow<List<Long>> {
        return favoritesRepository.getFavoriteIds()
    }
}
