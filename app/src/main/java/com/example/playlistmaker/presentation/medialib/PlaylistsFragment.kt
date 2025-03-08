package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.presentation.medialib.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.medialib.view.PlaylistsScreenState
import com.example.playlistmaker.presentation.medialib.view.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var newPlaylistButton: Button
    private lateinit var mediatekaIsEmpty: ImageView
    private lateinit var noCreatedPlaylists: TextView
    private lateinit var createdPlaylists: RecyclerView
    private lateinit var playlistCreatedNotify: TextView

    private lateinit var adapter: PlaylistsAdapter

    companion object {
        fun newInstance(): PlaylistsFragment = PlaylistsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newPlaylistButton = view.findViewById(R.id.newPlaylist)
        mediatekaIsEmpty = view.findViewById(R.id.mediatekaIsEmpty)
        noCreatedPlaylists = view.findViewById(R.id.noCreatedPlaylists)
        createdPlaylists = view.findViewById(R.id.createdPlaylists)
        playlistCreatedNotify = view.findViewById(R.id.playlistCreatedNotify)

        adapter = PlaylistsAdapter { playlist ->
            // Переход на экран «Плейлист»
            val bundle = Bundle().apply {
                putLong("PLAYLIST_ID_ARG", playlist.playlistId)
            }
            findNavController().navigate(
                R.id.action_libraryFragment_to_playlistInfoFragment,
                bundle
            )
        }
        createdPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        createdPlaylists.adapter = adapter

        newPlaylistButton.setOnClickListener {
            val dialog = CreatePlaylistFragment.newInstance()
            dialog.show(parentFragmentManager, "CreatePlaylistDialog")
        }

        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            renderState(state)
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    private fun renderState(state: PlaylistsScreenState) {
        if (state.playlists.isEmpty()) {
            mediatekaIsEmpty.visibility = View.VISIBLE
            noCreatedPlaylists.visibility = View.VISIBLE
            createdPlaylists.visibility = View.GONE
        } else {
            mediatekaIsEmpty.visibility = View.GONE
            noCreatedPlaylists.visibility = View.GONE
            createdPlaylists.visibility = View.VISIBLE
            adapter.updateList(state.playlists)
        }
    }
}
