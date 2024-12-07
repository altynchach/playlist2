package com.example.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.search.SearchActivity
import com.example.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "MainActivity started")

        val searchButton = findViewById<Button>(R.id.button_search)
        searchButton.setOnClickListener {
            Log.d("MainActivity", "Search button clicked")
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val settingsButton = findViewById<Button>(R.id.button_settings)
        settingsButton.setOnClickListener {
            Log.d("MainActivity", "Settings button clicked")
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
