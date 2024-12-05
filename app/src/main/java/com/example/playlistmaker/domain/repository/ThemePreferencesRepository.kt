package com.example.playlistmaker.domain.repository

interface ThemePreferencesRepository {
    fun isDarkMode(): Boolean
    fun setDarkMode(isDarkMode: Boolean)
    fun hasUserChangedTheme(): Boolean
}
