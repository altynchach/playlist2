package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.search.SearchActivity
import com.example.playlistmaker.Creator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var handler: Handler
    private lateinit var currentTimeText: TextView
    private var isPlaying = false
    private lateinit var track: Track

    private lateinit var playerInteractor: PlayerInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerInteractor = Creator.providePlayerInteractor()

        playButton = findViewById(R.id.buttonPlay)
        currentTimeText = findViewById(R.id.current_time)
        handler = Handler(Looper.getMainLooper())

        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        backButton.setOnClickListener { finish() }

        initializeTrackInfo()
        initializePlayer()
        setupPlayButton()
    }

    private fun initializeTrackInfo() {
        val title = findViewById<TextView>(R.id.title)
        val author = findViewById<TextView>(R.id.author)
        val durationSong = findViewById<TextView>(R.id.durationSong)
        val albumSong = findViewById<TextView>(R.id.albumSong)
        val yearSong = findViewById<TextView>(R.id.yearSong)
        val genreSong = findViewById<TextView>(R.id.genreSong)
        val countrySong = findViewById<TextView>(R.id.countrySong)
        val cover = findViewById<ImageView>(R.id.cover)

        val type = object : TypeToken<Track>() {}.type
        track = Gson().fromJson(intent.getStringExtra(SearchActivity.NAME_TRACK), type)

        title.text = track.trackName
        author.text = track.artistName

        durationSong.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)

        albumSong.text = track.collectionName ?: ""
        yearSong.text = if (track.releaseDate!!.length >= 4) track.releaseDate!!.substring(0, 4) else ""
        genreSong.text = track.primaryGenreName ?: ""
        countrySong.text = track.country ?: ""

        val imgSource = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(imgSource)
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.placeholder_max)
            .into(cover)
    }

    private fun initializePlayer() {
        try {
            track.previewUrl?.let { url ->
                playerInteractor.setTrackPreview(url)
            } ?: run {
                Toast.makeText(this, "No preview available", Toast.LENGTH_SHORT).show()
            }

            playerInteractor.setOnCompletionListener {
                stopPlayback()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load track", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPlayButton() {
        playButton.setOnClickListener {
            if (isPlaying) {
                pausePlayback()
            } else {
                startPlayback()
            }
        }
    }

    private fun startPlayback() {
        playerInteractor.play()
        isPlaying = true
        playButton.setImageResource(R.drawable.pause)
        updateProgress()
    }

    private fun pausePlayback() {
        playerInteractor.pause()
        isPlaying = false
        playButton.setImageResource(R.drawable.button_play)
        handler.removeCallbacksAndMessages(null)
    }

    private fun stopPlayback() {
        playerInteractor.stop()
        isPlaying = false
        playButton.setImageResource(R.drawable.button_play)
        currentTimeText.text = "00:00"
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateProgress() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isPlaying) {
                    val currentPositionMs = playerInteractor.getCurrentPosition()
                    val currentTimeSec = currentPositionMs / 1000
                    val minutes = currentTimeSec / 60
                    val seconds = currentTimeSec % 60
                    currentTimeText.text = String.format("%02d:%02d", minutes, seconds)
                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerInteractor.release()
        handler.removeCallbacksAndMessages(null)
    }
}
