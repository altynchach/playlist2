package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.dto.TracksResponse
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.data.network.ITunesApiService
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackRepositoryImpl(private val apiService: ITunesApiService) : TrackRepository {
    override fun searchTracks(
        query: String,
        onSuccess: (List<Track>) -> Unit,
        onFailure: () -> Unit
    ) {
        apiService.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful && response.body()?.results != null) {
                    val trackDtos = response.body()!!.results
                    val tracks = trackDtos.map { TrackMapper.mapDtoToDomain(it) }
                    onSuccess(tracks)
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                onFailure()
            }
        })
    }
}

