package com.example.playlistmaker.data.preferences

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository

class ThemePreferencesRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    ThemePreferencesRepository {
    private val darkMode = "DARK_MODE"

    override fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean(darkMode, false)
    }

    override fun setDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean(darkMode, isDarkMode).apply()
    }

    override fun hasUserChangedTheme(): Boolean {
        return sharedPreferences.contains(darkMode)
    }
}
