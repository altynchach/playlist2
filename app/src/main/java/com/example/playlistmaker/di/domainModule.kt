package com.example.playlistmaker.di

import com.example.playlistmaker.domain.interactor.*
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

    single<FavoritesInteractor> {
        FavoritesInteractorImpl(favoritesRepository = get())
    }
}
