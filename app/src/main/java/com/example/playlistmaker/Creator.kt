package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.network.ITunesApiService
import com.example.playlistmaker.data.preferences.ThemePreferencesRepositoryImpl
import com.example.playlistmaker.data.repository.PlayerRepositoryImpl
import com.example.playlistmaker.data.preferences.SearchHistoryPreferencesRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.interactor.*
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {

    private const val ITUNES_URL = "https://itunes.apple.com"
    private const val PREFERENCES = "com.example.playlistmaker.PREFERENCES"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ITUNES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideApiService(): ITunesApiService {
        return retrofit.create(ITunesApiService::class.java)
    }

    fun provideTrackRepository(): TrackRepository {
        return TrackRepositoryImpl(provideApiService())
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        val sharedPreferences = context.getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        )
        return SearchHistoryPreferencesRepositoryImpl(sharedPreferences)
    }

    fun provideSearchInteractor(context: Context): SearchInteractor {
        return SearchInteractorImpl(
            provideTrackRepository(),
            provideSearchHistoryRepository(context)
        )
    }

    fun provideThemeInteractor(context: Context): ThemeInteractor {
        return ThemeInteractorImpl(provideThemePreferencesRepository(context))
    }

    private fun provideThemePreferencesRepository(context: Context): ThemePreferencesRepository {
        val sharedPreferences = context.getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        )
        return ThemePreferencesRepositoryImpl(sharedPreferences)
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(PlayerRepositoryImpl())
    }
}
