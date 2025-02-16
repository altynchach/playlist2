package com.example.playlistmaker.data.favorites.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.data.favorites.dao.FavoriteTrackDao
import com.example.playlistmaker.data.favorites.entity.FavoriteTrackEntity

@Database(entities = [FavoriteTrackEntity::class], version = 1, exportSchema = false)
abstract class PlaylistMakerDatabase : RoomDatabase() {
    abstract fun favoriteTrackDao(): FavoriteTrackDao
}
