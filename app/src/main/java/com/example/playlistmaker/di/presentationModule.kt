package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.main.MainViewModel
import com.example.playlistmaker.presentation.medialib.view.LikedTracksViewModel
import com.example.playlistmaker.presentation.medialib.view.MediaViewModel
import com.example.playlistmaker.presentation.medialib.view.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.PlayerViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { MainViewModel() }
    viewModel { SearchViewModel(searchInteractor = get()) }
    viewModel { SettingsViewModel(themeInteractor = get()) }
    viewModel { PlayerViewModel(
        playerInteractor = get(),
        favoritesInteractor = get(),
        playlistInteractor = get())
    }
    viewModel { MediaViewModel() }
    viewModel { LikedTracksViewModel(favoritesInteractor = get()) }
    viewModel { PlaylistsViewModel(playlistInteractor = get()) }
}
