package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.PlayerScreenState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "name"
    }

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var playButton: ImageButton
    private lateinit var currentTimeText: TextView

    private lateinit var title: TextView
    private lateinit var author: TextView
    private lateinit var durationSong: TextView
    private lateinit var albumSong: TextView
    private lateinit var yearSong: TextView
    private lateinit var genreSong: TextView
    private lateinit var countrySong: TextView
    private lateinit var cover: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        playButton = findViewById(R.id.buttonPlay)
        currentTimeText = findViewById(R.id.current_time)

        title = findViewById(R.id.title)
        author = findViewById(R.id.author)
        durationSong = findViewById(R.id.durationSong)
        albumSong = findViewById(R.id.albumSong)
        yearSong = findViewById(R.id.yearSong)
        genreSong = findViewById(R.id.genreSong)
        countrySong = findViewById(R.id.countrySong)
        cover = findViewById(R.id.cover)

        backButton.setOnClickListener { finish() }

        val type = object : TypeToken<Track>() {}.type
        val track: Track = Gson().fromJson(intent.getStringExtra(NAME_TRACK), type)
        viewModel.setTrack(track)

        playButton.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }

        viewModel.getState().observe(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: PlayerScreenState) {
        val track = state.track ?: return

        title.text = track.trackName
        author.text = track.artistName
        durationSong.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        albumSong.text = track.collectionName ?: ""
        yearSong.text = track.releaseDate?.substring(0,4) ?: ""
        genreSong.text = track.primaryGenreName ?: ""
        countrySong.text = track.country ?: ""

        val imgSource = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(imgSource)
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.placeholder_max)
            .into(cover)

        currentTimeText.text = state.currentTimeFormatted
        playButton.setImageResource(if (state.isPlaying) R.drawable.pause else R.drawable.button_play)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onDestroy()
    }
}
