package com.example.playlistmaker.data.repository

import android.content.SharedPreferences
import com.example.playlistmaker.data.dto.TrackDto
import com.example.playlistmaker.data.mappers.TrackMapper
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistoryRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SearchHistoryRepository {

    companion object {
        private const val SEARCH_HISTORY_KEY = "SEARCH_HISTORY"
        private const val MAX_HISTORY_SIZE = 10
    }

    private val gson = Gson()
    private val type = object : TypeToken<ArrayList<TrackDto>>() {}.type

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val trackDtos: List<TrackDto> = gson.fromJson(json, type)
            trackDtos.map { TrackMapper.mapDtoToDomain(it) }
        } else {
            emptyList()
        }
    }

    override fun saveTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }
        val trackDtos = history.map { TrackMapper.mapDomainToDto(it) }
        val json = gson.toJson(trackDtos)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }
}

class SearchHistoryRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SearchHistoryRepository {

    private val gson = Gson()
    private val historyKey = "search_history"
    private val maxHistorySize = 6

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<TrackDto>>() {}.type
            val trackDtos: List<TrackDto> = gson.fromJson(json, type)
            trackDtos.map { TrackMapper.mapDtoToDomain(it) }
        } else {
            emptyList()
        }
    }

    override fun saveTrack(track: Track) {
        val history = getHistory().toMutableList()

        history.removeAll { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > maxHistorySize) {
            history.removeAt(history.size - 1)
        }
        val trackDtos = history.map { TrackMapper.mapDomainToDto(it) }
        sharedPreferences.edit().putString(historyKey, gson.toJson(trackDtos)).apply()
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }
}