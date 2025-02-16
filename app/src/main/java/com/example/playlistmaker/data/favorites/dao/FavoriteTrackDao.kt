package com.example.playlistmaker.data.favorites.dao

import androidx.room.*
import com.example.playlistmaker.data.favorites.entity.FavoriteTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTrackDao {

    @Query("SELECT * FROM favorite_tracks ORDER BY timeAdded DESC")
    fun getAllFavorites(): Flow<List<FavoriteTrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    fun getAllFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :id)")
    fun isFavorite(id: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTrack(track: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :id")
    suspend fun deleteFavoriteTrack(id: Long)
}
