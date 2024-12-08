package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.presentation.utils.ThemeManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val themeInteractor = Creator.provideThemeInteractor(applicationContext)
        val isDarkMode = themeInteractor.shouldApplyDarkTheme()
        ThemeManager.applyTheme(isDarkMode)
    }
}
