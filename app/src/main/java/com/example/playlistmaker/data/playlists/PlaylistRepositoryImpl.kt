package com.example.playlistmaker.data.playlists

import com.example.playlistmaker.data.playlists.dao.PlaylistDao
import com.example.playlistmaker.data.playlists.entity.PlaylistEntity
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.repository.PlaylistRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistRepositoryImpl(private val playlistDao: PlaylistDao) : PlaylistRepository {

    private val gson = Gson()
    private val typeToken = object : TypeToken<ArrayList<Long>>() {}.type

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entityList ->
            entityList.map { entity -> mapEntityToDomain(entity) }
        }
    }

    override suspend fun createPlaylist(playlist: Playlist): Long {
        val entity = mapDomainToEntity(playlist)
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(mapDomainToEntity(playlist))
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

        if (currentIds.contains(trackId)) {
            return false
        }

        currentIds.add(trackId)
        val newCount = playlistEntity.trackCount + 1

        val updatedEntity = playlistEntity.copy(
            trackIds = gson.toJson(currentIds),
            trackCount = newCount
        )
        playlistDao.insertPlaylist(updatedEntity)
        return true
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
