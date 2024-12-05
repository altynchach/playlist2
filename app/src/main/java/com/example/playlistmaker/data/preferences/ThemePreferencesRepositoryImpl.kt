package com.example.playlistmaker.data.preferences

import android.content.SharedPreferences
import com.example.playlistmaker.domain.repository.ThemePreferencesRepository

class ThemePreferencesRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    ThemePreferencesRepository {

    override fun isDarkMode(): Boolean {
        return sharedPreferences.getBoolean("DARK_MODE", false)
    }

    override fun setDarkMode(isDarkMode: Boolean) {
        sharedPreferences.edit().putBoolean("DARK_MODE", isDarkMode).apply()
    }

    override fun hasUserChangedTheme(): Boolean {
        return sharedPreferences.contains("DARK_MODE")
    }
}
