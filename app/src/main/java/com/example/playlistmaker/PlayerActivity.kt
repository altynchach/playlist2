package com.example.playlistmaker

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.media.MediaPlayer
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
import com.example.playlistmaker.SearchActivity.Companion.NAME_TRACK
import com.example.playlistmaker.recyclerView.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var playButton: ImageButton
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var handler: Handler
    private lateinit var currentTimeText: TextView
    private var isPlaying = false
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Настройка отображения в полноэкранном режиме
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playButton = findViewById(R.id.buttonPlay)
        currentTimeText = findViewById(R.id.current_time)
        handler = Handler(Looper.getMainLooper())

        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        backButton.setOnClickListener { finish() }

        initializeTrackInfo()
        initializeMediaPlayer()
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
        track = Gson().fromJson(intent.getStringExtra(NAME_TRACK), type)

        title.text = track.trackName
        author.text = track.artistName
        durationSong.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        albumSong.text = track.collectionName
        yearSong.text = track.releaseDate.substring(0, 4)
        genreSong.text = track.primaryGenreName
        countrySong.text = track.country
        val imgSource = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(imgSource)
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.placeholder_max)
            .into(cover)
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setOnCompletionListener {
                stopPlayback()
            }
        }

        try {
            mediaPlayer.setDataSource(track.previewUrl)
            mediaPlayer.prepare()
        } catch (e: IOException) {
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
        mediaPlayer.start()
        isPlaying = true
        playButton.setImageResource(R.drawable.pause)
        updateProgress()
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
        isPlaying = false
        playButton.setImageResource(R.drawable.button_play)
        handler.removeCallbacksAndMessages(null)
    }

    private fun stopPlayback() {
        isPlaying = false
        playButton.setImageResource(R.drawable.button_play)
        currentTimeText.text = "00:00"
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateProgress() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isPlaying) {
                    val currentTime = mediaPlayer.currentPosition / 1000
                    val minutes = currentTime / 60
                    val seconds = currentTime % 60
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
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}
