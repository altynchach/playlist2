package com.example.playlistmaker.presentation

import android.app.Application
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.utils.ThemeManager

class App : Application() {

    private lateinit var themeInteractor: ThemeInteractor

    override fun onCreate() {
        super.onCreate()

        themeInteractor = Creator.provideThemeInteractor(applicationContext)

        val isDarkMode = themeInteractor.shouldApplyDarkTheme()
        ThemeManager.applyTheme(isDarkMode)
    }
}
