package com.example.playlistmaker.domain.models

data class Playlist(
    val playlistId: Long = 0,
    val name: String,
    val description: String,
    val coverFilePath: String?,
    val trackIds: List<Long>,
    val trackCount: Int
)
