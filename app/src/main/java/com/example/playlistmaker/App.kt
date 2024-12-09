package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.di.appModule
import com.example.playlistmaker.di.dataModule
import com.example.playlistmaker.di.domainModule
import com.example.playlistmaker.di.presentationModule
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.utils.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule, dataModule, domainModule, presentationModule)
        }
        val themeInteractor: ThemeInteractor = org.koin.java.KoinJavaComponent.getKoin().get()
        val isDarkMode = themeInteractor.shouldApplyDarkTheme()
        ThemeManager.applyTheme(isDarkMode)
    }
}
