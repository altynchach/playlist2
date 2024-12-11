package com.example.playlistmaker.di

import android.media.MediaPlayer
import com.example.playlistmaker.data.network.ITunesApiService
import com.example.playlistmaker.data.preferences.SearchHistoryPreferencesRepositoryImpl
import com.example.playlistmaker.data.preferences.ThemePreferencesRepositoryImpl
import com.example.playlistmaker.data.repository.PlayerRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.repository.PlayerRepository
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.dsl.module

private const val ITUNES_URL = "https://itunes.apple.com"

val dataModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(ITUNES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
    }
    single<ITunesApiService> { get<Retrofit>().create(ITunesApiService::class.java) }
    single<TrackRepository> { TrackRepositoryImpl(apiService = get()) }
    single<SearchHistoryRepository> { SearchHistoryPreferencesRepositoryImpl(sharedPreferences = get()) }
    single<ThemePreferencesRepository> { ThemePreferencesRepositoryImpl(sharedPreferences = get()) }

    single { MediaPlayer() }
    single<PlayerRepository> { PlayerRepositoryImpl(mediaPlayer = get()) }
}
