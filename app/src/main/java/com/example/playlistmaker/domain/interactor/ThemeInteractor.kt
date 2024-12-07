package com.example.playlistmaker.domain.interactor

interface ThemeInteractor {
    fun applyTheme(isDarkMode: Boolean)
    fun shouldApplyDarkTheme(): Boolean
    fun isDarkMode(): Boolean
    fun setDarkMode(isDarkMode: Boolean)
}
