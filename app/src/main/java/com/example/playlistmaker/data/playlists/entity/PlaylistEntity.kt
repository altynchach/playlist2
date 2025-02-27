package com.example.playlistmaker.data.playlists.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,
    val playlistName: String,
    val description: String?,
    val coverFilePath: String?,
    val trackIds: String?,
    val trackCount: Int
)
