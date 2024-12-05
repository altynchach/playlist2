package com.example.playlistmaker.domain


class ThemePreferencesRepository {
    interface ThemePreferencesRepository {
        fun isDarkMode(): Boolean
        fun setDarkMode(isDarkMode: Boolean)
        fun hasUserChangedTheme(): Boolean
    }
}