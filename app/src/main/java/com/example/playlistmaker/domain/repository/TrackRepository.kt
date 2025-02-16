package com.example.playlistmaker.domain.repository

import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface TrackRepository {

    fun searchTracksFlow(query: String): Flow<List<Track>>
}
