// PlaylistInfoFragment.kt
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
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
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

class PlaylistInfoFragment : DialogFragment() {

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

    // Views in the included layout on the second (menu) sheet
    private lateinit var playlistImageSheet: ImageView
    private lateinit var playlistNameSheet: TextView
    private lateinit var playlistCountTracksSheet: TextView

    private var isClickAllowed = true
    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (current) {
            isClickAllowed = false
            view?.postDelayed({ isClickAllowed = true }, 1000L)
        }
        return current
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
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_FullScreenDialog)
        isCancelable = false

        playlistIdArg = arguments?.getLong(PLAYLIST_ID_ARG, 0L) ?: 0L
        viewModel.loadPlaylist(playlistIdArg)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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

        // Main bottom sheet (tracks)
        playlistSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaylistInfo)
        playlistSheetBehavior.isHideable = false
        bottomSheetPlaylistInfo.visibility = View.VISIBLE
        playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        view.post { anchorSheetUnderButtons() }

        // Second bottom sheet (menu)
        editSheetBehavior = BottomSheetBehavior.from(bottomSheetEditPlaylistInfo)
        editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // reference included views on second sheet
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
                // we simply pop ourselves
                parentFragmentManager.popBackStack()
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
                bottomSheetEditPlaylistInfo.bringToFront()
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

        // observe changes
        viewModel.state.observe(viewLifecycleOwner, Observer { s ->
            renderState(s)
            updateSecondSheetHeader(s)
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylist(playlistIdArg)
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun anchorSheetUnderButtons() {
        val loc = IntArray(2)
        buttonsLayout.getLocationOnScreen(loc)
        val btnY = loc[1]
        val screenHeightPx = resources.displayMetrics.heightPixels
        val marginPx = resources.getDimensionPixelSize(R.dimen.size24)
        val dist = screenHeightPx - (btnY + buttonsLayout.height + marginPx)
        playlistSheetBehavior.peekHeight = dist
        playlistSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {}
            override fun onSlide(sheet: View, slideOffset: Float) {
                // keep it anchored
                if (slideOffset < 0) {
                    playlistSheetBehavior.peekHeight = dist
                }
            }
        })
        playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun renderState(s: PlaylistInfoScreenState) {
        val pl = s.playlist ?: return
        // cover
        if (pl.coverFilePath.isNullOrEmpty()) {
            playlistImage.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(pl.coverFilePath)
                .placeholder(R.drawable.placeholder)
                .into(playlistImage)
        }
        // name & desc
        playlistName.text = pl.name
        playlistDescription.isVisible = pl.description.isNotBlank()
        playlistDescription.text = pl.description

        // track count
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
        // total time
        sumLength.text = s.totalDuration
        // fill track list
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
            .setTitle(R.string.delete_track)
            .setMessage(R.string.delete_track_question)
            .setPositiveButton(R.string.delete) { dialog, _ ->
                dialog.dismiss()
                viewModel.removeTrackFromPlaylist(track)
            }
            .setNegativeButton(R.string.cancel) { d, _ ->
                d.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_question)
            .setPositiveButton(R.string.yes) { dd, _ ->
                dd.dismiss()
                viewModel.deletePlaylist()
                // after removing from DB, simply pop ourselves from the fragment stack
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton(R.string.no) { d, _ ->
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
