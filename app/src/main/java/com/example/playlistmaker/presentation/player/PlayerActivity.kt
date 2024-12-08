package com.example.playlistmaker.presentation.player

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.PlayerScreenState
import com.example.playlistmaker.presentation.utils.dpToPx
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_player.*
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "name"
    }

    private val viewModel: PlayerViewModel by viewModels { PlayerViewModel.Factory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val type = object : TypeToken<Track>() {}.type
        val track: Track = Gson().fromJson(intent.getStringExtra(NAME_TRACK), type)

        viewModel.setTrack(track)

        buttonBack.setOnClickListener { finish() }

        buttonPlay.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }

        viewModel.state.observe(this) { state ->
            renderState(state)
        }
    }

    private fun renderState(state: PlayerScreenState) {
        val track = state.track ?: return

        title = track.trackName
        titleView.text = track.trackName
        author.text = track.artistName
        durationSong.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTime)
        albumSong.text = track.collectionName ?: ""
        yearSong.text = track.releaseDate?.take(4) ?: ""
        genreSong.text = track.primaryGenreName ?: ""
        countrySong.text = track.country ?: ""

        val imgSource = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(imgSource)
            .centerInside()
            .transform(RoundedCorners(dpToPx(8f, this)))
            .placeholder(R.drawable.placeholder_max)
            .into(cover)

        current_time.text = state.currentTimeFormatted
        if (state.isPlaying) {
            buttonPlay.setImageResource(R.drawable.pause)
        } else {
            buttonPlay.setImageResource(R.drawable.button_play)
        }
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
