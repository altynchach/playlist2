package com.example.playlistmaker

import android.content.SharedPreferences
import com.example.playlistmaker.recyclerView.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val SEARCH_HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    fun addTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()

        history.removeIf { it.trackName == track.trackName }

        history.add(0, track)

        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }

        saveSearchHistory(history)
    }

    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(SEARCH_HISTORY_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            Gson().fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(SEARCH_HISTORY_KEY).apply()
    }

    private fun saveSearchHistory(history: List<Track>) {
        val json = Gson().toJson(history)
        sharedPreferences.edit().putString(SEARCH_HISTORY_KEY, json).apply()
    }
}
