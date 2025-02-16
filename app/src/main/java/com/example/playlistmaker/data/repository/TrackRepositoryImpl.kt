package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ITunesApiService
import com.example.playlistmaker.data.dto.TracksResponse
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class TrackRepositoryImpl(
    private val apiService: ITunesApiService,
    private val favoritesRepository: FavoritesRepository
) : TrackRepository {

    override fun searchTracksFlow(query: String): Flow<List<Track>> = flow {
        try {
            val response = apiService.searchSuspend(query)
            val tracks = response.results.map { TrackMapper.mapDtoToDomain(it) }

            val favoriteIds = favoritesRepository.getFavoriteIds().first()

            emit(tracks)
        } catch (e: IOException) {
            emit(emptyList())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
