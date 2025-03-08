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
        val pEnt = playlistDao.getPlaylistById(playlistId) ?: return false
        val currentIds: ArrayList<Long> = if (!pEnt.trackIds.isNullOrEmpty()) {
            gson.fromJson(pEnt.trackIds, typeToken)
        } else {
            arrayListOf()
        }
        if (currentIds.contains(trackId)) return false
        currentIds.add(0, trackId)
        val updatedEntity = pEnt.copy(
            trackIds = gson.toJson(currentIds),
            trackCount = currentIds.size
        )
        playlistDao.insertPlaylist(updatedEntity)
        return true
    }

    override suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long): Boolean {
        val pEnt = playlistDao.getPlaylistById(playlistId) ?: return false
        val curr: ArrayList<Long> = if (!pEnt.trackIds.isNullOrEmpty()) {
            gson.fromJson(pEnt.trackIds, typeToken)
        } else {
            arrayListOf()
        }
        if (!curr.remove(trackId)) return false
        val upEnt = pEnt.copy(
            trackIds = if (curr.isEmpty()) null else gson.toJson(curr),
            trackCount = curr.size
        )
        playlistDao.updatePlaylist(upEnt)

        // check if that track is used anywhere else
        val allPL = playlistDao.getAllPlaylists().first()
        var used = false
        for (pl in allPL) {
            if (!pl.trackIds.isNullOrEmpty()) {
                val ids = gson.fromJson<List<Long>>(pl.trackIds, typeToken)
                if (ids.contains(trackId)) {
                    used = true
                    break
                }
            }
        }
        val isFav = favoritesRepository.isFavorite(trackId).first()
        if (!used && !isFav) {
            favoriteTrackDao.deleteFavoriteTrack(trackId)
        }
        return true
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return
        if (!entity.trackIds.isNullOrEmpty()) {
            val curr: ArrayList<Long> = gson.fromJson(entity.trackIds, typeToken)
            playlistDao.deletePlaylist(playlistId)

            val all = playlistDao.getAllPlaylists().first()
            for (id in curr) {
                var stillUsed = false
                for (pl in all) {
                    if (!pl.trackIds.isNullOrEmpty()) {
                        val ids: List<Long> = gson.fromJson(pl.trackIds, typeToken)
                        if (ids.contains(id)) {
                            stillUsed = true
                            break
                        }
                    }
                }
                val isFav = favoritesRepository.isFavorite(id).first()
                if (!stillUsed && !isFav) {
                    favoriteTrackDao.deleteFavoriteTrack(id)
                }
            }
        } else {
            // if no tracks
            playlistDao.deletePlaylist(playlistId)
        }
    }

    private fun mapEntityToDomain(entity: PlaylistEntity): Playlist {
        val tIds: List<Long> = if (!entity.trackIds.isNullOrEmpty()) {
            gson.fromJson(entity.trackIds, typeToken)
        } else emptyList()
        return Playlist(
            playlistId = entity.playlistId,
            name = entity.playlistName,
            description = entity.description.orEmpty(),
            coverFilePath = entity.coverFilePath,
            trackIds = tIds,
            trackCount = entity.trackCount
        )
    }

    private fun mapDomainToEntity(domain: Playlist): PlaylistEntity {
        val tJson = if (domain.trackIds.isNotEmpty()) gson.toJson(domain.trackIds) else null
        return PlaylistEntity(
            playlistId = domain.playlistId,
            playlistName = domain.name,
            description = domain.description.ifBlank { null },
            coverFilePath = domain.coverFilePath,
            trackIds = tJson,
            trackCount = domain.trackCount
        )
    }
}
