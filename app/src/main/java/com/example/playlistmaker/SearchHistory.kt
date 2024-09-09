package com.example.playlistmaker

import android.content.SharedPreferences
import com.example.playlistmaker.recyclerView.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val historyKey = "search_history"
    private val maxHistorySize = 10

    // Fetch the search history from SharedPreferences
    fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    // Save a track to the search history, ensuring no duplicates and a maximum of 10 tracks
    fun saveTrack(track: Track) {
        val history = getHistory().toMutableList()

        // Remove the track if it's already in the history
        history.remove(track)

        // Add the track to the top of the list
        history.add(0, track)

        // Ensure that the history does not exceed 10 tracks
        if (history.size > maxHistorySize) {
            history.removeAt(history.size - 1)
        }

        // Save the updated history back to SharedPreferences
        sharedPreferences.edit().putString(historyKey, gson.toJson(history)).apply()
    }

    // Clear the entire search history
    fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }
}
