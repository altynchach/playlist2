package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.recyclerView.Track
import com.example.playlistmaker.Utils.dpToPx
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val KEY_FOR_INTENT_DATA = "Selected track"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val backToPrevScreenButton = findViewById<ImageView>(R.id.ivBackToPrevScr)
        val songCover = findViewById<ImageView>(R.id.ivSongCover)
        val songTitle = findViewById<TextView>(R.id.tvSongTitle)
        val artistName = findViewById<TextView>(R.id.tvAuthorOfSong)
        val trackDuration = findViewById<TextView>(R.id.tvTrackTimeChanging)
        val album = findViewById<TextView>(R.id.tvAlbumNameChanging)
        val groupOfAlbumInfo = findViewById<Group>(R.id.gAlbumInfo)
        val yearOfSoundPublished = findViewById<TextView>(R.id.tvYearOfSongChanging)
        val genreOfSong = findViewById<TextView>(R.id.tvGenreChanging)
        val countryOfSong = findViewById<TextView>(R.id.tvCountryOfSongChanging)

        backToPrevScreenButton.setOnClickListener { finish() }

        val json: String? = intent.getStringExtra(KEY_FOR_INTENT_DATA)
        if (json != null) {
            val currentTrack: Track = Gson().fromJson(json, Track::class.java)
            Glide.with(this)
                .load(currentTrack.artworkUrl100.replace("100x100bb.jpg", "512x512bb.jpg"))
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(dpToPx(8f, this)))
                .into(songCover)
            songTitle.text = currentTrack.trackName
            artistName.text = currentTrack.artistName
            trackDuration.text = SimpleDateFormat(
                "mm:ss",
                Locale.getDefault()
            ).format(
                currentTrack.trackTime
            )
            groupOfAlbumInfo.isVisible = currentTrack.collectionName != null
            album.text = currentTrack.collectionName
            yearOfSoundPublished.text = currentTrack.releaseDate.split("-")[0]
            genreOfSong.text = currentTrack.primaryGenreName
            countryOfSong.text = currentTrack.country
        } else {
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_SHORT).show()
        }
    }
}