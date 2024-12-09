package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.network.NetworkModule
import com.example.playlistmaker.data.preferences.SearchHistoryPreferencesRepositoryImpl
import com.example.playlistmaker.data.preferences.ThemePreferencesRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractorImpl
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractorImpl
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository
import com.example.playlistmaker.domain.repository.TrackRepository

object Creator {

    private const val PREFERENCES = "com.example.playlistmaker.PREFERENCES"

    private fun provideSharedPreferences(context: Context) =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun provideTrackRepository(context: Context): TrackRepository {
        return TrackRepositoryImpl(NetworkModule.apiService)
    }

    fun provideSearchHistoryRepository(context: Context): SearchHistoryRepository {
        return SearchHistoryPreferencesRepositoryImpl(provideSharedPreferences(context))
    }

    fun provideSearchInteractor(context: Context): SearchInteractor {
        return SearchInteractorImpl(
            provideTrackRepository(context),
            provideSearchHistoryRepository(context)
        )
    }

    fun provideThemeInteractor(context: Context): ThemeInteractor {
        return ThemeInteractorImpl(provideThemePreferencesRepository(context))
    }

    private fun provideThemePreferencesRepository(context: Context): ThemePreferencesRepository {
        return ThemePreferencesRepositoryImpl(provideSharedPreferences(context))
    }
}
