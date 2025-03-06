package com.example.playlistmaker.presentation.medialib.playlistinfo

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.CreatePlaylistFragment
import com.example.playlistmaker.presentation.medialib.playlistinfo.adapter.PlaylistTracksAdapter
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
    private lateinit var bottomSheetPlaylistInfo: LinearLayout
    private lateinit var bottomSheetEditPlaylistInfo: LinearLayout
    private lateinit var playlistTracksRecyclerBS: RecyclerView
    private lateinit var darkFrame: View

    private lateinit var playlistTracksAdapter: PlaylistTracksAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val playlistId = arguments?.getLong(PLAYLIST_ID_ARG, 0L) ?: 0L
        viewModel.loadPlaylist(playlistId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.playlist_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        backFromPlaylistInfo = view.findViewById(R.id.backFromPlaylistInfo)
        playlistImage = view.findViewById(R.id.PlaylistImage)
        playlistName = view.findViewById(R.id.PlaylistName)
        playlistDescription = view.findViewById(R.id.PlaylistDescription)
        sumLength = view.findViewById(R.id.sumLength)
        tracksCount = view.findViewById(R.id.tracksCount)
        sharePlaylist = view.findViewById(R.id.sharePlaylist)
        editPlaylist = view.findViewById(R.id.editPlaylist)
        bottomSheetPlaylistInfo = view.findViewById(R.id.bottom_sheet_playlist_info)
        bottomSheetEditPlaylistInfo = view.findViewById(R.id.bottom_sheet_edit_playlist_info)
        playlistTracksRecyclerBS = view.findViewById(R.id.playlistTracksRecyclerBS)
        darkFrame = view.findViewById(R.id.darkFrame)

        // Инициализация адаптера
        playlistTracksAdapter = PlaylistTracksAdapter { track ->
            openPlayerActivity(track)
        }
        playlistTracksAdapter.setOnTrackLongClickListener { track ->
            showRemoveTrackDialog(track)
        }
        playlistTracksRecyclerBS.layoutManager = LinearLayoutManager(requireContext())
        playlistTracksRecyclerBS.adapter = playlistTracksAdapter

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetPlaylistInfo)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val editSheetBehavior = BottomSheetBehavior.from(bottomSheetEditPlaylistInfo)
        editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val shareLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.share)
        val editLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.edit)
        val deleteLayout = bottomSheetEditPlaylistInfo.findViewById<View>(R.id.delete)

        shareLayout.setOnClickListener {
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.sharePlaylist {
                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.no_tracks_in_playlist_to_share), Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, it)
                    startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                }
            }
        }
        editLayout.setOnClickListener {
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.state.value?.playlist?.let { pl ->
                openEditPlaylistScreen(pl.playlistId)
            }
        }
        deleteLayout.setOnClickListener {
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        backFromPlaylistInfo.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        sharePlaylist.setOnClickListener {
            viewModel.sharePlaylist {
                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.no_tracks_in_playlist_to_share), Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, it)
                    startActivity(Intent.createChooser(intent, getString(R.string.share_playlist)))
                }
            }
        }

        editPlaylist.setOnClickListener {
            darkFrame.visibility = View.VISIBLE
            editSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        darkFrame.setOnClickListener {
            darkFrame.visibility = View.INVISIBLE
            editSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        editSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    darkFrame.visibility = View.INVISIBLE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            renderState(state)
        })
    }

    private fun renderState(state: PlaylistInfoScreenState) {
        val playlist = state.playlist ?: return
        if (playlist.coverFilePath.isNullOrEmpty()) {
            playlistImage.setImageResource(R.drawable.placeholder)
        } else {
            Glide.with(this)
                .load(playlist.coverFilePath)
                .placeholder(R.drawable.placeholder)
                .into(playlistImage)
        }
        playlistName.text = playlist.name
        playlistDescription.text = playlist.description
        playlistDescription.visibility = if (playlist.description.isBlank()) View.GONE else View.VISIBLE

        val tracksSize = playlist.trackIds.size
        if (tracksSize == 0) {
            tracksCount.text = getString(R.string.no_tracks_in_playlist)
        } else {
            val countText = resources.getQuantityString(R.plurals.playlist_tracks_count, tracksSize, tracksSize)
            tracksCount.text = countText
        }
        sumLength.text = state.totalDuration

        playlistTracksAdapter.updateTracks(state.tracks)
    }

    private fun openPlayerActivity(track: Track) {
        val ctx = context ?: return
        val intent = Intent(ctx, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.NAME_TRACK, com.google.gson.Gson().toJson(track))
        startActivity(intent)
    }

    private fun showRemoveTrackDialog(track: Track) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_track))
            .setMessage(getString(R.string.delete_track_question))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                viewModel.removeTrackFromPlaylist(track)
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeletePlaylistDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_playlist))
            .setMessage(getString(R.string.delete_playlist_question))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                dialog.dismiss()
                viewModel.deletePlaylist()
                // Закроем фрагмент, вернёмся к списку плейлистов
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun openEditPlaylistScreen(playlistId: Long) {
        val dialog = CreatePlaylistFragment.newInstance(playlistId)
        dialog.show(parentFragmentManager, "EditPlaylistDialog")
    }
}
