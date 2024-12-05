package com.example.playlistmaker

import android.content.Context
import com.example.playlistmaker.data.preferences.ThemePreferencesRepositoryImpl
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractorImpl
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository

object Creator {

    fun provideThemeInteractor(context: Context): ThemeInteractor {
        return ThemeInteractorImpl(provideThemePreferencesRepository(context))
    }

    private fun provideThemePreferencesRepository(context: Context): ThemePreferencesRepository {
        val sharedPreferences = context.getSharedPreferences(
            "com.example.playlistmaker.PREFERENCES",
            Context.MODE_PRIVATE
        )
        return ThemePreferencesRepositoryImpl(sharedPreferences)
    }
}
