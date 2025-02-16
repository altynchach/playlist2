package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.favorites.dao.FavoriteTrackDao
import com.example.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val dao: FavoriteTrackDao
) : FavoritesRepository {

    override fun getFavorites(): Flow<List<Track>> {
        return dao.getAllFavorites().map { entities ->
            entities.map { entityToTrack(it) }
        }
    }

    override fun isFavorite(trackId: Long): Flow<Boolean> {
        return dao.isFavorite(trackId)
    }

    override suspend fun addFavorite(track: Track) {
        dao.insertFavoriteTrack(trackToEntity(track))
    }

    override suspend fun removeFavorite(trackId: Long) {
        dao.deleteFavoriteTrack(trackId)
    }

    override fun getFavoriteIds(): Flow<List<Long>> {
        return dao.getAllFavoriteIds()
    }

    private fun entityToTrack(entity: FavoriteTrackEntity): Track {
        return Track(
            trackId = entity.trackId,
            trackName = entity.trackName,
            artistName = entity.artistName,
            trackTime = entity.trackTime,
            artworkUrl100 = entity.artworkUrl100,
            collectionName = entity.collectionName,
            releaseDate = entity.releaseDate,
            primaryGenreName = entity.primaryGenreName,
            country = entity.country,
            previewUrl = entity.previewUrl
        )
    }

    private fun trackToEntity(track: Track): FavoriteTrackEntity {
        return FavoriteTrackEntity(
            trackId = track.trackId,
            trackName = track.trackName,
            artistName = track.artistName,
            trackTime = track.trackTime,
            artworkUrl100 = track.artworkUrl100,
            collectionName = track.collectionName,
            releaseDate = track.releaseDate,
            primaryGenreName = track.primaryGenreName,
            country = track.country,
            previewUrl = track.previewUrl,
            timeAdded = System.currentTimeMillis()
        )
    }
}
