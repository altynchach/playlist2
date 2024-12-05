package com.example.playlistmaker.presentation

import android.app.Application
import android.content.SharedPreferences
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.utils.ThemeManager

class App : Application(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var themeInteractor: ThemeInteractor
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        themeInteractor = Creator.provideThemeInteractor(applicationContext)

        sharedPreferences = getSharedPreferences(
            "com.example.playlistmaker.PREFERENCES",
            MODE_PRIVATE
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val isDarkMode = themeInteractor.shouldApplyDarkTheme()
        ThemeManager.applyTheme(isDarkMode)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "DARK_MODE") {
            val isDarkMode = sharedPreferences?.getBoolean("DARK_MODE", false) ?: false
            ThemeManager.applyTheme(isDarkMode)
        }
    }
}
