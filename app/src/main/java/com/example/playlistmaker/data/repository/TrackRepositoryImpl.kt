package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ITunesApiService
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository

class TrackRepositoryImpl(private val apiService: ITunesApiService) : TrackRepository {

    override suspend fun searchTracks(query: String): Result<List<Track>> {
        return try {
            val response = apiService.search(query)
            val tracks = response.results.map { TrackMapper.mapDtoToDomain(it) }
            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
