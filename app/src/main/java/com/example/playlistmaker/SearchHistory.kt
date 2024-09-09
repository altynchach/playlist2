package com.example.playlistmaker

import android.content.SharedPreferences
import com.example.playlistmaker.recyclerView.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()
    private val historyKey = "search_history"
    private val maxHistorySize = 10

    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveTrack(track: Track) {
        val history = getHistory().toMutableList()
        history.remove(track)
        history.add(0, track)
        if (history.size > maxHistorySize) {
            history.removeAt(history.size - 1)
        }
        sharedPreferences.edit().putString(historyKey, gson.toJson(history)).apply()
    }

    fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }
}
