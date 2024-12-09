package com.example.playlistmaker.di

import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchInteractor
import com.example.playlistmaker.domain.interactor.SearchInteractorImpl
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.domain.interactor.ThemeInteractorImpl
import org.koin.dsl.module

val domainModule = module {
    single<SearchInteractor> {
        SearchInteractorImpl(
            trackRepository = get(),
            searchHistoryRepository = get()
        )
    }
    single<ThemeInteractor> {
        ThemeInteractorImpl(themePreferencesRepository = get())
    }
    single<PlayerInteractor> {
        PlayerInteractorImpl(playerRepository = get())
    }
}
