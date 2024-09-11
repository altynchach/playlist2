package com.example.playlistmaker

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

open class BaseActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("com.example.playlistmaker.PREFERENCES", MODE_PRIVATE)

        loadSavedTheme()
    }

    private fun loadSavedTheme() {
        val hasUserChangedTheme = sharedPreferences.contains("DARK_MODE")

        if (hasUserChangedTheme) {
            val isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false)
            applyTheme(isDarkMode)
        } else {
            val isSystemInDarkMode = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            applyTheme(isSystemInDarkMode)
            sharedPreferences.edit().putBoolean("DARK_MODE", isSystemInDarkMode).apply()
        }
    }

    protected fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
