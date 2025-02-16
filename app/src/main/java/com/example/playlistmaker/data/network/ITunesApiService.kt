package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TracksResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {

    // @GET("/search?entity=song")
    // fun search(@Query("term") term: String): Call<TracksResponse>

    @GET("/search?entity=song")
    suspend fun searchSuspend(@Query("term") term: String): TracksResponse
}