package com.example.playlistmaker.data.favorites.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.favorites.dao.FavoriteTrackDao
import com.example.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import com.example.playlistmaker.data.playlists.dao.PlaylistDao
import com.example.playlistmaker.data.playlists.dao.PlaylistTrackDao
import com.example.playlistmaker.data.playlists.entity.PlaylistEntity
import com.example.playlistmaker.data.playlists.entity.PlaylistTrackEntity

@Database(
    entities = [
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PlaylistMakerDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao
}
