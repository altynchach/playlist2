package com.example.playlistmaker.di

import com.example.playlistmaker.presentation.main.MainViewModel
import com.example.playlistmaker.presentation.medialib.LikedTracksViewModel
import com.example.playlistmaker.presentation.medialib.MediaViewModel
import com.example.playlistmaker.presentation.medialib.PlaylistsViewModel
import com.example.playlistmaker.presentation.player.PlayerViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel { MainViewModel() }
    viewModel { SearchViewModel(searchInteractor = get()) }
    viewModel { SettingsViewModel(themeInteractor = get()) }
    viewModel { PlayerViewModel(playerInteractor = get()) }
    viewModel { MediaViewModel() }
    viewModel { LikedTracksViewModel() }
    viewModel { PlaylistsViewModel() }
}
