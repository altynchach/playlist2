package com.example.playlistmaker.retrofit

import android.telecom.Call
import androidx.contentpager.content.Query
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApi {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String) : Call<TracksResponse>
}