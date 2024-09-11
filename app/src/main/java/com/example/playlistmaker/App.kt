package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // Получаем SharedPreferences
        val sharedPreferences = getSharedPreferences("com.example.playlistmaker.PREFERENCES", MODE_PRIVATE)

        // Проверяем, сохранено ли значение темы в SharedPreferences
        if (!sharedPreferences.contains("DARK_MODE")) {
            // Если нет, то определяем тему устройства по умолчанию и сохраняем её
            val isSystemInDarkMode = (resources.configuration.uiMode
                    and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            sharedPreferences.edit().putBoolean("DARK_MODE", isSystemInDarkMode).apply()
        }

        // Загружаем сохранённую тему
        val isDarkMode = sharedPreferences.getBoolean("DARK_MODE", false)
        applyTheme(isDarkMode)  // Применяем тему
    }

    private fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
