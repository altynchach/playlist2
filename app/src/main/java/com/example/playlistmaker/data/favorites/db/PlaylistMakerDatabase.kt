package com.example.playlistmaker.data.favorites.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.favorites.dao.FavoriteTrackDao
import com.example.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import com.example.playlistmaker.data.playlists.dao.PlaylistDao
import com.example.playlistmaker.data.playlists.entity.PlaylistEntity

@Database(
    entities = [FavoriteTrackEntity::class, PlaylistEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PlaylistMakerDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao

    abstract fun playlistDao(): PlaylistDao
}
