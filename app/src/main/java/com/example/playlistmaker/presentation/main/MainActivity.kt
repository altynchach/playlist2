package com.example.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.MediaActivity
import com.example.playlistmaker.presentation.search.SearchActivity
import com.example.playlistmaker.presentation.settings.SettingsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.button_search)
        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val settingsButton = findViewById<Button>(R.id.button_settings)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        val libraryButton = findViewById<Button>(R.id.button_library)
        libraryButton.setOnClickListener {
            // При клике запускаем MediaActivity
            startActivity(Intent(this, MediaActivity::class.java))
        }
    }
}
