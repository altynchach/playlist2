package com.example.playlistmaker.presentation.player

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.CreatePlaylistFragment
import com.example.playlistmaker.presentation.medialib.PlaylistsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.presentation.medialib.view.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "TRACK_DATA"
    }

    private val viewModel: PlayerViewModel by viewModel()
    private val playlistsViewModel: PlaylistsViewModel by viewModel()

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

    private lateinit var likeButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton

    // НОВОЕ:
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var overlay: View
    private lateinit var playlistsRecyclerBS: RecyclerView
    private lateinit var newPlaylistBS: ImageButton
    private lateinit var playlistsAdapterBS: PlaylistsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val backButton = findViewById<ImageButton>(R.id.buttonBack)
        playButton = findViewById(R.id.buttonPlay)
        currentTimeText = findViewById(R.id.current_time)
        likeButton = findViewById(R.id.addToLikes)
        addToPlaylistButton = findViewById(R.id.addToPlaylist)

        title = findViewById(R.id.title)
        author = findViewById(R.id.author)
        durationSong = findViewById(R.id.durationSong)
        albumSong = findViewById(R.id.albumSong)
        yearSong = findViewById(R.id.yearSong)
        genreSong = findViewById(R.id.genreSong)
        countrySong = findViewById(R.id.countrySong)
        cover = findViewById(R.id.cover)

        overlay = findViewById(R.id.overlay)
        val bottomSheet = findViewById<View>(R.id.playlists_bottom_sheet)
        playlistsRecyclerBS = findViewById(R.id.playlistsRecyclerBS)
        newPlaylistBS = findViewById(R.id.newPlaylistBS)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        overlay.visibility = View.GONE
                    }
                    else -> {
                        overlay.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        playlistsAdapterBS = PlaylistsAdapter { playlist ->
            val track = viewModel.getState().value?.track ?: return@PlaylistsAdapter
            viewModel.addTrackToPlaylist(playlist.playlistId, track.trackId) { added, playlistName ->
                if (added) {
                    Toast.makeText(
                        this,
                        getString(R.string.added_to_playlist, playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.already_in_playlist, playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        playlistsRecyclerBS.layoutManager = LinearLayoutManager(this)
        playlistsRecyclerBS.adapter = playlistsAdapterBS

        newPlaylistBS.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val fragment = CreatePlaylistFragment.newInstance()
            fragment.show(supportFragmentManager, "CreatePlaylistDialog")
        }

        // Загрузка плейлистов из playlistsViewModel
        playlistsViewModel.state.observe(this) { playlistsState ->
            playlistsAdapterBS.updateList(playlistsState.playlists)
        }

        backButton.setOnClickListener { finish() }

        val json = intent.getStringExtra(NAME_TRACK)
        if (json.isNullOrEmpty()) {
            finish()
            return
        }

        val track = try {
            Gson().fromJson(json, Track::class.java)
        } catch (e: Exception) {
            finish()
            return
        }

        viewModel.setTrack(track)

        playButton.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }

        likeButton.setOnClickListener {
            viewModel.onLikeButtonClicked()
        }

        addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            playlistsViewModel.loadPlaylists() // подгружаем список
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
        yearSong.text = track.releaseDate?.take(4)
        genreSong.text = track.primaryGenreName ?: ""
        countrySong.text = track.country ?: ""

        val artworkUrl = track.artworkUrl100 ?: ""
        val imgSource = if (artworkUrl.contains("/")) {
            artworkUrl.replaceAfterLast('/', "512x512bb.jpg")
        } else {
            artworkUrl
        }

        Glide.with(this)
            .load(imgSource)
            .centerInside()
            .transform(RoundedCorners(8))
            .placeholder(R.drawable.placeholder_max)
            .into(cover)

        currentTimeText.text = state.currentTimeFormatted
        playButton.setImageResource(
            if (state.isPlaying) R.drawable.pause else R.drawable.button_play
        )

        if (state.isFavorite) {
            likeButton.setImageResource(R.drawable.like_button_active)
        } else {
            likeButton.setImageResource(R.drawable.add_to_likes)
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
