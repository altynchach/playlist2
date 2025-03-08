// PlaylistRepositoryImpl.kt
package com.example.playlistmaker.data.playlists

import com.example.playlistmaker.data.favorites.dao.FavoriteTrackDao
import com.example.playlistmaker.data.playlists.dao.PlaylistDao
import com.example.playlistmaker.data.playlists.entity.PlaylistEntity
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.repository.FavoritesRepository
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val favoritesRepository: FavoritesRepository,
    private val favoriteTrackDao: FavoriteTrackDao
) : PlaylistRepository {

    private val gson = Gson()
    private val typeToken = object : TypeToken<ArrayList<Long>>() {}.type

    override fun getAllPlaylists() =
        playlistDao.getAllPlaylists().map { entities ->
            entities.map { mapEntityToDomain(it) }
        }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = mapDomainToEntity(playlist)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        val entity = mapDomainToEntity(playlist)
        playlistDao.updatePlaylist(entity)
    }

    override suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null
        return mapEntityToDomain(entity)
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long): Boolean {
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return false
        val currentIds: ArrayList<Long> = if (!playlistEntity.trackIds.isNullOrEmpty()) {
            gson.fromJson(playlistEntity.trackIds, typeToken)
        } else {
            arrayListOf()
        }
        if (currentIds.contains(trackId)) return false
        currentIds.add(0, trackId)
        val updatedEntity = playlistEntity.copy(
            trackIds = gson.toJson(currentIds),
            trackCount = currentIds.size
        )
        playlistDao.insertPlaylist(updatedEntity)
        return true
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean {
        val playlistEntity = playlistDao.getPlaylistById(playlistId) ?: return false
        val currentIds: ArrayList<Long> = if (!playlistEntity.trackIds.isNullOrEmpty()) {
            gson.fromJson(playlistEntity.trackIds, typeToken)
        } else {
            arrayListOf()
        }
        if (!currentIds.remove(trackId)) return false
        val updatedEntity = playlistEntity.copy(
            trackIds = if (currentIds.isEmpty()) null else gson.toJson(currentIds),
            trackCount = currentIds.size
        )
        playlistDao.updatePlaylist(updatedEntity)

        val allPlaylists = playlistDao.getAllPlaylists().first()
        var trackStillUsed = false
        for (pl in allPlaylists) {
            if (!pl.trackIds.isNullOrEmpty()) {
                val ids: List<Long> = gson.fromJson(pl.trackIds, typeToken)
                if (ids.contains(trackId)) {
                    trackStillUsed = true
                    break
                }
            }
        }
        val isFav = favoritesRepository.isFavorite(trackId).first()
        if (!trackStillUsed && !isFav) {
            favoriteTrackDao.deleteFavoriteTrack(trackId)
        }
        return true
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return
        if (!entity.trackIds.isNullOrEmpty()) {
            val currentIds: ArrayList<Long> = gson.fromJson(entity.trackIds, typeToken)
            playlistDao.deletePlaylist(playlistId)

            val allPlaylists = playlistDao.getAllPlaylists().first()
            for (id in currentIds) {
                var trackStillUsed = false
                for (pl in allPlaylists) {
                    if (!pl.trackIds.isNullOrEmpty()) {
                        val ids: List<Long> = gson.fromJson(pl.trackIds, typeToken)
                        if (ids.contains(id)) {
                            trackStillUsed = true
                            break
                        }
                    }
                }
                val isFav = favoritesRepository.isFavorite(id).first()
                if (!trackStillUsed && !isFav) {
                    favoriteTrackDao.deleteFavoriteTrack(id)
                }
            }
        } else {
            playlistDao.deletePlaylist(playlistId)
        }
    }

    private fun mapEntityToDomain(entity: PlaylistEntity): Playlist {
        val trackIdsList: List<Long> = if (!entity.trackIds.isNullOrEmpty()) {
            gson.fromJson(entity.trackIds, typeToken)
        } else {
            emptyList()
        }
        return Playlist(
            playlistId = entity.playlistId,
            name = entity.playlistName,
            description = entity.description.orEmpty(),
            coverFilePath = entity.coverFilePath,
            trackIds = trackIdsList,
            trackCount = entity.trackCount
        )
    }

    private fun mapDomainToEntity(domain: Playlist): PlaylistEntity {
        val trackIdsJson = if (domain.trackIds.isNotEmpty()) {
            gson.toJson(domain.trackIds)
        } else {
            null
        }
        return PlaylistEntity(
            playlistId = domain.playlistId,
            playlistName = domain.name,
            description = domain.description.ifBlank { null },
            coverFilePath = domain.coverFilePath,
            trackIds = trackIdsJson,
            trackCount = domain.trackCount
        )
    }
}
