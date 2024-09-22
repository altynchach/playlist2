package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.util.Log

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "MainActivity started") // Log the start of the activity

        val searchButton = findViewById<Button>(R.id.button_search)
        searchButton.setOnClickListener {
            Log.d("MainActivity", "Search button clicked") // Log button click
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
