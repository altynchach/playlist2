package com.example.playlistmaker.presentation.medialib

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.adapter.PlaylistTracksAdapter
import com.example.playlistmaker.presentation.medialib.view.PlaylistInfoScreenState
import com.example.playlistmaker.presentation.medialib.view.PlaylistInfoViewModel
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistInfoFragment : Fragment() {

    private val viewModel: PlaylistInfoViewModel by viewModel()

    private lateinit var backFromPlaylistInfo: ImageView
    private lateinit var playlistImage: ImageView
    private lateinit var playlistName: TextView
    private lateinit var playlistDescription: TextView
    private lateinit var sumLength: TextView
    private lateinit var tracksCount: TextView
    private lateinit var sharePlaylist: ImageView
    private lateinit var editPlaylist: ImageView
    private lateinit var buttonsLayout: LinearLayout

    private lateinit var bottomSheetPlaylistInfo: LinearLayout
    private lateinit var bottomSheetEditPlaylistInfo: LinearLayout
    private lateinit var playlistTracksRecyclerBS: RecyclerView
    private lateinit var darkFrame: View

    private lateinit var playlistTracksAdapter: PlaylistTracksAdapter
    private var playlistIdArg: Long = 0L

    // second sheet views (если нужно)
    private lateinit var playlistImageSheet: ImageView
    private lateinit var playlistNameSheet: TextView
    private lateinit var playlistCountTracksSheet: TextView

    private var isClickAllowed = true
    private fun clickDebounce(): Boolean {
        val c = isClickAllowed
        if (c) {
            isClickAllowed = false
            view?.postDelayed({ isClickAllowed = true }, 1000L)
        }
        return c
    }

    companion object {
        private const val PLAYLIST_ID_ARG = "PLAYLIST_ID_ARG"
        fun newInstance(playlistId: Long): PlaylistInfoFragment {
            val fragment = PlaylistInfoFragment()
            val args = Bundle()
            args.putLong(PLAYLIST_ID_ARG, playlistId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var playlistSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var editSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistIdArg = arguments?.getLong(PLAYLIST_ID_ARG, 0L) ?: 0L
        viewModel.loadPlaylist(playlistIdArg)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Теперь обычный Fragment (не DialogFragment)
        return inflater.inflate(R.layout.playlist_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backFromPlaylistInfo = view.findViewById(R.id.backFromPlaylistInfo)
        playlistImage = view.findViewById(R.id.PlaylistImage)
        playlistName = view.findViewById(R.id.PlaylistName)
        playlistDescription = view.findViewById(R.id.PlaylistDescription)
        sumLength = view.findViewById(R.id.sumLength)
        tracksCount = view.findViewById(R.id.tracksCount)
        sharePlaylist = view.findViewById(R.id.sharePlaylist)
        editPlaylist = view.findViewById(R.id.editPlaylist)
        buttonsLayout = view.findViewById(R.id.buttonsLayout)

        bottomSheetPlaylistInfo = view.findViewById(R.id.bottom_sheet_playlist_info)
        bottomSheetEditPlaylistInfo = view.findViewById(R.id.bottom_sheet_edit_playlist_info)
        darkFrame = view.findViewById(R.id.darkFrame)
        playlistTracksRecyclerBS = view.findViewById(R.id.playlistTracksRecyclerBS)

        playlistTracksAdapter = PlaylistTracksAdapter { track ->
            if (clickDebounce()) {
                openPlayerActivity(track)
            }
        }.also {
            it.setOnTrackLongClickListener { track ->
                if (clickDebounce()) {
                    showRemoveTrackDialog(track)
                }
            }
        }
        playlistTracksRecyclerBS.layoutManager = LinearLayoutManager(requireContext())
        playlistTracksRecyclerBS.adapter = playlistTracksAdapter

        // main sheet
        playlistSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaylistInfo)
        playlistSheetBehavior.isHideable = false
        bottomSheetPlaylistInfo.visibility = View.VISIBLE
        playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // second sheet
        editSheetBehavior = BottomSheetBehavior.from(bottomSheetEditPlaylistInfo)
        editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Если нужно получить ссылки на Image/Text из второго bottomSheet:
        playlistImageSheet = bottomSheetEditPlaylistInfo.findViewById(R.id.playlistImageSheet)
        playlistNameSheet = bottomSheetEditPlaylistInfo.findViewById(R.id.playlistNameSheet)
        playlistCountTracksSheet = bottomSheetEditPlaylistInfo.findViewById(R.id.playlistCountTracksSheet)

        editSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    darkFrame.visibility = View.INVISIBLE
                }
            }
            override fun onSlide(sheet: View, slideOffset: Float) {}
        })

        backFromPlaylistInfo.setOnClickListener {
            if (clickDebounce()) {
                // Оборачиваем popBackStack в try/catch
                try {
                    findNavController().popBackStack()
                } catch (e: IllegalStateException) {
                    // Если NavController нет, fallback:
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }

        sharePlaylist.setOnClickListener {
            if (clickDebounce()) {
                viewModel.sharePlaylist { shareText ->
                    if (shareText.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_tracks_in_playlist_to_share),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                    }
                }
            }
        }

        editPlaylist.setOnClickListener {
            if (clickDebounce()) {
                bottomSheetEditPlaylistInfo.visibility = View.VISIBLE
                darkFrame.visibility = View.VISIBLE
                editSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        darkFrame.setOnClickListener {
            darkFrame.visibility = View.INVISIBLE
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        val shareLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.share)
        val editLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.edit)
        val deleteLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.delete)

        shareLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.sharePlaylist { shareText ->
                    if (shareText.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_tracks_in_playlist_to_share),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                    }
                }
            }
        }

        editLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                viewModel.state.value?.playlist?.let { pl ->
                    val dialog = CreatePlaylistFragment.newInstance(pl.playlistId)
                    // Если экран "CreatePlaylistFragment" тоже в NavGraph, можно navigate()
                    // Если это Dialog, то show(...)
                    dialog.show(parentFragmentManager, "EditPlaylistDialog")
                }
            }
        }

        deleteLayout.setOnClickListener {
            if (clickDebounce()) {
                editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                showDeletePlaylistDialog()
            }
        }

        viewModel.state.observe(viewLifecycleOwner, Observer { s ->
            renderState(s)
            updateSecondSheetHeader(s)
        })

        // После инициализации
        view.post { anchorSheetUnderButtons() }
    }

    override fun onResume() {
        super.onResume()
        // Если возвращаемся из редактирования, перезагружаем
        viewModel.loadPlaylist(playlistIdArg)
    }

    private fun anchorSheetUnderButtons() {
        val loc = IntArray(2)
        buttonsLayout.getLocationOnScreen(loc)
        val btnY = loc[1]
        val screenHeightPx = resources.displayMetrics.heightPixels
        val marginPx = resources.getDimensionPixelSize(R.dimen.size24)
        val distFromBottom = screenHeightPx - (btnY + buttonsLayout.height + marginPx)
        playlistSheetBehavior.peekHeight = distFromBottom
        playlistSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {}
            override fun onSlide(sheet: View, slideOffset: Float) {
                if (slideOffset < 0) {
                    playlistSheetBehavior.peekHeight = distFromBottom
                }
            }
        })
        playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun renderState(s: PlaylistInfoScreenState) {
        val pl = s.playlist ?: return
        if (pl.coverFilePath.isNullOrEmpty()) {
            playlistImage.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(pl.coverFilePath)
                .placeholder(R.drawable.placeholder)
                .into(playlistImage)
        }
        playlistName.text = pl.name
        playlistDescription.isVisible = pl.description.isNotBlank()
        playlistDescription.text = pl.description
        if (pl.trackIds.isEmpty()) {
            tracksCount.text = getString(R.string.no_tracks_in_playlist)
        } else {
            val cntText = resources.getQuantityString(
                R.plurals.playlist_tracks_count,
                pl.trackIds.size,
                pl.trackIds.size
            )
            tracksCount.text = cntText
        }
        sumLength.text = s.totalDuration
        playlistTracksAdapter.updateTracks(s.tracks)
    }

    private fun updateSecondSheetHeader(s: PlaylistInfoScreenState) {
        val pl = s.playlist ?: return
        if (pl.coverFilePath.isNullOrEmpty()) {
            playlistImageSheet.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(pl.coverFilePath)
                .placeholder(R.drawable.placeholder)
                .into(playlistImageSheet)
        }

        playlistNameSheet.text = pl.name
        if (pl.trackIds.isEmpty()) {
            playlistCountTracksSheet.text = getString(R.string.no_tracks_in_playlist)
        } else {
            val cText = resources.getQuantityString(
                R.plurals.playlist_tracks_count,
                pl.trackIds.size,
                pl.trackIds.size
            )
            playlistCountTracksSheet.text = cText
        }
    }

    private fun showRemoveTrackDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_track))
            .setMessage(getString(R.string.delete_track_question))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                viewModel.removeTrackFromPlaylist(track)
            }
            .setNegativeButton(getString(R.string.cancel)) { d, _ ->
                d.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.delete_playlist_question))
            .setPositiveButton(getString(R.string.yes)) { dd, _ ->
                dd.dismiss()
                viewModel.deletePlaylist()
                // При попытке вернуться назад, если NavController нет -> fallback
                try {
                    findNavController().popBackStack()
                } catch (e: IllegalStateException) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            .setNegativeButton(getString(R.string.no)) { d, _ ->
                d.dismiss()
            }
            .create()
            .show()
    }

    private fun openPlayerActivity(track: Track) {
        val ctx = context ?: return
        val intent = Intent(ctx, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.NAME_TRACK, com.google.gson.Gson().toJson(track))
        startActivity(intent)
    }
}
