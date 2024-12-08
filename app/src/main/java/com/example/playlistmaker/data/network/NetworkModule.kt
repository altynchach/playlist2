package com.example.playlistmaker.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val ITUNES_URL = "https://itunes.apple.com"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ITUNES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ITunesApiService by lazy {
        retrofit.create(ITunesApiService::class.java)
    }
}
