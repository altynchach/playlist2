package com.example.playlistmaker.presentation.search

import com.example.playlistmaker.domain.models.Track

data class SearchScreenState(
    val isLoading: Boolean = false,
    val showResults: Boolean = false,
    val showNothingFound: Boolean = false,
    val showError: Boolean = false,
    val showHistory: Boolean = false,
    val results: List<Track> = emptyList(),
    val history: List<Track> = emptyList()
)
