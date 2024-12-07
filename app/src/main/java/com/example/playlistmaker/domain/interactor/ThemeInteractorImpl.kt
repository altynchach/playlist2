package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.ThemePreferencesRepository

class ThemeInteractorImpl(
    private val themePreferencesRepository: ThemePreferencesRepository
) : ThemeInteractor {

    override fun applyTheme(isDarkMode: Boolean) {
        themePreferencesRepository.setDarkMode(isDarkMode)
    }

    override fun shouldApplyDarkTheme(): Boolean {
        return if (themePreferencesRepository.hasUserChangedTheme()) {
            themePreferencesRepository.isDarkMode()
        } else {
            false
        }
    }

    override fun isDarkMode(): Boolean {
        return themePreferencesRepository.isDarkMode()
    }

    override fun setDarkMode(isDarkMode: Boolean) {
        themePreferencesRepository.setDarkMode(isDarkMode)
    }
}
