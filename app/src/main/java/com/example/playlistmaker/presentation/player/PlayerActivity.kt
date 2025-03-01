package com.example.playlistmaker.presentation.player

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.CreatePlaylistFragment
import com.example.playlistmaker.presentation.medialib.PlaylistsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val NAME_TRACK = "TRACK_DATA"
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

    // BottomSheet для добавления в плейлист
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var bottomSheet: androidx.constraintlayout.widget.ConstraintLayout
    // Но в макете это не ConstraintLayout, а LinearLayout, так что:
    private lateinit var bottomSheetLinear: LinearLayout
    private lateinit var addNewPlaylistButton: ImageButton
    // У нас в разметке Button, а не ImageButton, так что точнее:
    private lateinit var addNewPlaylistPlayer: android.widget.Button

    private lateinit var playlistRecyclerBS: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: PlaylistsAdapter

    private var track: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Инициализация вёрстки
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

        // Кнопки
        backButton.setOnClickListener { finish() }
        playButton.setOnClickListener { viewModel.onPlayPauseClicked() }
        likeButton.setOnClickListener { viewModel.onLikeButtonClicked() }
        addToPlaylistButton.setOnClickListener {
            // Показать BottomSheet
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Настраиваем BottomSheet
        val bsLayout = findViewById<LinearLayout>(R.id.standard_bottom_sheet_player)
        bottomSheetBehavior = BottomSheetBehavior.from(bsLayout).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // RecyclerView и кнопка «Новый плейлист» внутри BottomSheet
        playlistRecyclerBS = findViewById(R.id.playlistRecyclerBSPlayer)
        addNewPlaylistPlayer = findViewById(R.id.addNewPlaylistPlayer)

        adapter = PlaylistsAdapter { playlist ->
            val currentTrack = track ?: return@PlaylistsAdapter
            lifecycleScope.launch {
                viewModel.addTrackToPlaylist(playlist.playlistId, currentTrack.trackId) { added, playlistName ->
                    if (added) {
                        Toast.makeText(
                            this@PlayerActivity,
                            getString(R.string.added_to_playlist, playlistName),
                            Toast.LENGTH_SHORT
                        ).show()
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
        playlistRecyclerBS.layoutManager = LinearLayoutManager(this)
        playlistRecyclerBS.adapter = adapter

        addNewPlaylistPlayer.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            // Открываем CreatePlaylistFragment (диалог)
            val fragment = CreatePlaylistFragment.newInstance()
            fragment.show(supportFragmentManager, "CreatePlaylistDialog")
        }

        // Подписываемся на изменения state
        viewModel.getState().observe(this) { state ->
            renderState(state)
        }

        // Забираем Track из Intent
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
        // Передаём во VM
        viewModel.setTrack(track!!)
    }

    override fun onResume() {
        super.onResume()
        // При возвращении (например, после создания нового плейлиста), подгрузим список плейлистов
        viewModel.loadPlaylistsForPlayerScreen { playlists ->
            adapter.updateList(playlists)
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
