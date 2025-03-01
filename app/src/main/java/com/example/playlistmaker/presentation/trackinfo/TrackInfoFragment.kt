package com.example.playlistmaker.presentation.trackinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.CreatePlaylistFragment
import com.example.playlistmaker.presentation.medialib.PlaylistsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.recyclerview.widget.RecyclerView

class TrackInfoFragment : Fragment() {

    companion object {
        private const val ARG_TRACK_JSON = "ARG_TRACK_JSON"

        fun newInstance(trackJson: String): TrackInfoFragment {
            val fragment = TrackInfoFragment()
            val args = Bundle()
            args.putString(ARG_TRACK_JSON, trackJson)
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: TrackInfoViewModel by viewModel()

    private lateinit var backFromTrackInfo: ImageView
    private lateinit var addToFavorTrackInfo: ImageButton
    private lateinit var playButtonTrackInfo: ImageButton
    private lateinit var likeSongTrackInfo: ImageButton
    private lateinit var mainTrackInfoImage: ImageView

    private lateinit var songCurrentTimeTrackInfo: TextView
    private lateinit var songNameTrackInfo: TextView
    private lateinit var authorNameTrackInfo: TextView
    private lateinit var songLengthTrackInfo: TextView
    private lateinit var songAlbumTrackInfo: TextView
    private lateinit var songYearTrackInfo: TextView
    private lateinit var songGenreTrackInfo: TextView
    private lateinit var songCountryTrackInfo: TextView

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var bottomSheet: LinearLayout
    private lateinit var addNewPlaylist: Button
    private lateinit var playlistRecyclerBS: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trackJson = arguments?.getString(ARG_TRACK_JSON, "") ?: ""
        if (trackJson.isNotEmpty()) {
            val track = Gson().fromJson(trackJson, Track::class.java)
            viewModel.setTrack(track)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.track_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backFromTrackInfo = view.findViewById(R.id.backFromTrackInfo)
        addToFavorTrackInfo = view.findViewById(R.id.addToFavorTrackInfo) // «Добавить в плейлист»
        playButtonTrackInfo = view.findViewById(R.id.playButtonTrackInfo)
        likeSongTrackInfo = view.findViewById(R.id.likeSongTrackInfo)
        mainTrackInfoImage = view.findViewById(R.id.mainTrackInfoImage)

        songCurrentTimeTrackInfo = view.findViewById(R.id.songCurrentTimeTrackInfo)
        songNameTrackInfo = view.findViewById(R.id.songNameTrackInfo)
        authorNameTrackInfo = view.findViewById(R.id.authorNameTrackInfo)
        songLengthTrackInfo = view.findViewById(R.id.songLengthTrackInfo)
        songAlbumTrackInfo = view.findViewById(R.id.songAlbumTrackInfo)
        songYearTrackInfo = view.findViewById(R.id.songYearTrackInfo)
        songGenreTrackInfo = view.findViewById(R.id.songGenreTrackInfo)
        songCountryTrackInfo = view.findViewById(R.id.songCountryTrackInfo)

        bottomSheet = view.findViewById(R.id.standard_bottom_sheet)
        addNewPlaylist = view.findViewById(R.id.addNewPlaylist)
        playlistRecyclerBS = view.findViewById(R.id.playlistRecyclerBS)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        val adapter = PlaylistsAdapter { playlist ->
            val track = viewModel.getState().value?.track ?: return@PlaylistsAdapter
            lifecycleScope.launch {
                val result = viewModel.addTrackToPlaylist(playlist.playlistId, track)
                if (result.added) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.added_to_playlist, result.playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.already_in_playlist, result.playlistName),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        playlistRecyclerBS.layoutManager = LinearLayoutManager(requireContext())
        playlistRecyclerBS.adapter = adapter

        addNewPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val fragment = CreatePlaylistFragment.newInstance()
            fragment.show(parentFragmentManager, "CreatePlaylistDialog")
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            adapter.updateList(playlists)
        }

        // Наблюдаем за основным состоянием трека
        viewModel.getState().observe(viewLifecycleOwner) { state ->
            renderState(state)
        }

        // Сразу грузим списки плейлистов
        viewModel.loadPlaylists()

        // Обработчики нажатий
        backFromTrackInfo.setOnClickListener {
            // Закрыть экран (фрагмент)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // «Добавить в плейлист»
        addToFavorTrackInfo.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        // Play / Pause
        playButtonTrackInfo.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }
        // Like
        likeSongTrackInfo.setOnClickListener {
            viewModel.onLikeButtonClicked()
        }
    }

    private fun renderState(state: TrackInfoScreenState) {
        val track = state.track ?: return

        // Заполняем поля
        songNameTrackInfo.text = track.trackName
        authorNameTrackInfo.text = track.artistName
        songAlbumTrackInfo.text = track.collectionName ?: ""
        songYearTrackInfo.text = track.releaseDate?.take(4) ?: ""
        songGenreTrackInfo.text = track.primaryGenreName ?: ""
        songCountryTrackInfo.text = track.country ?: ""
        songLengthTrackInfo.text = state.durationFormatted
        songCurrentTimeTrackInfo.text = state.currentTimeFormatted

        // Пример: подстановка обложки. Используйте Glide, если нужно
        // ...

        // Меняем иконку play/pause
        if (state.isPlaying) {
            playButtonTrackInfo.setImageResource(R.drawable.pause)
        } else {
            playButtonTrackInfo.setImageResource(R.drawable.play_song_button_mediateka)
        }

        // Меняем иконку лайка
        if (state.isFavorite) {
            likeSongTrackInfo.setImageResource(R.drawable.like_button_active)
        } else {
            likeSongTrackInfo.setImageResource(R.drawable.like_mediateka)
        }
    }
}
