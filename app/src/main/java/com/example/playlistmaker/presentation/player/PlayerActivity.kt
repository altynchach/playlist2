package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.CreatePlaylistFragment
import com.example.playlistmaker.presentation.medialib.adapter.BottomSheetPlaylistsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "TRACK_DATA"
        private const val PLAYLIST_CREATED_KEY = "PLAYLIST_CREATED"
    }

    private val viewModel: PlayerViewModel by viewModel()

    private lateinit var backButton: ImageButton
    private lateinit var playButton: ImageButton
    private lateinit var currentTimeText: TextView
    private lateinit var likeButton: ImageButton
    private lateinit var addToPlaylistButton: ImageButton

    private lateinit var title: TextView
    private lateinit var author: TextView
    private lateinit var durationSong: TextView
    private lateinit var albumSong: TextView
    private lateinit var yearSong: TextView
    private lateinit var genreSong: TextView
    private lateinit var countrySong: TextView
    private lateinit var cover: ImageView

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetLayout: LinearLayout
    private lateinit var playlistRecyclerBSPlayer: RecyclerView
    private lateinit var addNewPlaylistPlayer: Button

    private lateinit var bottomSheetAdapter: BottomSheetPlaylistsAdapter

    private var track: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val scrimOverlay = findViewById<View>(R.id.scrimOverlay)

        backButton = findViewById(R.id.buttonBack)
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

        backButton.setOnClickListener { finish() }
        playButton.setOnClickListener { viewModel.onPlayPauseClicked() }
        likeButton.setOnClickListener { viewModel.onLikeButtonClicked() }
        addToPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        bottomSheetLayout = findViewById(R.id.standard_bottom_sheet_player)
        bottomSheetLayout.setBackgroundResource(R.drawable.rounded_bottom_sheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        scrimOverlay.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        scrimOverlay.visibility = View.GONE
                    }
                    else -> {}
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset >= 0) {
                    scrimOverlay.alpha = slideOffset
                }
            }
        })

        playlistRecyclerBSPlayer = findViewById(R.id.playlistRecyclerBSPlayer)
        addNewPlaylistPlayer = findViewById(R.id.addNewPlaylistPlayer)

        bottomSheetAdapter = BottomSheetPlaylistsAdapter { playlist ->
            val currentTrack = track ?: return@BottomSheetPlaylistsAdapter
            lifecycleScope.launch {
                viewModel.addTrackToPlaylist(playlist.playlistId, currentTrack.trackId) { added, playlistName ->
                    if (added) {
                        val inflater = LayoutInflater.from(this@PlayerActivity)
                        val customToastView = inflater.inflate(
                            R.layout.playlist_created_toast,
                            findViewById(android.R.id.content),
                            false
                        )
                        val toastTextView: TextView = customToastView.findViewById(R.id.playlistCreatedNotify)
                        toastTextView.text = getString(R.string.added_to_playlist, playlistName)

                        val toast = Toast(applicationContext).apply {
                            duration = Toast.LENGTH_SHORT
                            view = customToastView
                        }
                        toast.show()

                        viewModel.loadPlaylistsForPlayerScreen { updatedPlaylists ->
                            bottomSheetAdapter.updateList(updatedPlaylists)
                        }

                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        Toast.makeText(
                            this@PlayerActivity,
                            getString(R.string.already_in_playlist, playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        playlistRecyclerBSPlayer.layoutManager = LinearLayoutManager(this)
        playlistRecyclerBSPlayer.adapter = bottomSheetAdapter

        addNewPlaylistPlayer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val fragment = CreatePlaylistFragment.newInstance()
            fragment.show(supportFragmentManager, "CreatePlaylistDialog")
        }

        supportFragmentManager.setFragmentResultListener(PLAYLIST_CREATED_KEY, this) { _, _ ->
            viewModel.loadPlaylistsForPlayerScreen { playlists ->
                bottomSheetAdapter.updateList(playlists)
            }
        }

        viewModel.getState().observe(this) { state ->
            renderState(state)
        }

        val json = intent.getStringExtra(NAME_TRACK)
        Log.d("PlayerActivity", "Received JSON: $json")

        if (json.isNullOrEmpty()) {
            finish()
            return
        }

        track = try {
            Gson().fromJson(json, Track::class.java)
        } catch (e: Exception) {
            Log.e("PlayerActivity", "Error parsing track: ${e.message}", e)
            null
        }
        if (track == null) {
            finish()
            return
        }

        viewModel.setTrack(track!!)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylistsForPlayerScreen { playlists ->
            bottomSheetAdapter.updateList(playlists)
        }
    }

    private fun renderState(state: PlayerScreenState) {
        val stTrack = state.track ?: return

        title.text = stTrack.trackName ?: ""
        author.text = stTrack.artistName ?: ""
        durationSong.text = stTrack.trackTime.let {
            val format = SimpleDateFormat("mm:ss", Locale.getDefault())
            format.format(it)
        }
        albumSong.text = stTrack.collectionName ?: ""
        yearSong.text = stTrack.releaseDate?.take(4) ?: ""
        genreSong.text = stTrack.primaryGenreName ?: ""
        countrySong.text = stTrack.country ?: ""

        val artworkUrl = stTrack.artworkUrl100 ?: ""
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
        likeButton.setImageResource(
            if (state.isFavorite) R.drawable.like_button_active else R.drawable.add_to_likes
        )
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
