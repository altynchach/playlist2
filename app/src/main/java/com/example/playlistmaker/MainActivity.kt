package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.emoji.text.EmojiCompat
import androidx.emoji.bundled.BundledEmojiCompatConfig

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация EmojiCompat
        val config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)

        val searchButton = findViewById<Button>(R.id.button_search)
        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        val libraryButton = findViewById<Button>(R.id.button_library)
        libraryButton.setOnClickListener {
            // Действие для кнопки библиотеки
        }

        val settingsButton = findViewById<Button>(R.id.button_settings)
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}
