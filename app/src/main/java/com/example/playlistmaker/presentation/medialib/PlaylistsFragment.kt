package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.adapter.PlaylistsAdapter
import com.example.playlistmaker.presentation.medialib.view.PlaylistsScreenState
import com.example.playlistmaker.presentation.medialib.view.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    companion object {
        fun newInstance() = PlaylistsFragment()
        const val PLAYLIST_CREATED_KEY = "PLAYLIST_CREATED"
    }

    private val viewModel: PlaylistsViewModel by viewModel()

    private lateinit var newPlaylistButton: Button
    private lateinit var mediatekaIsEmpty: ImageView
    private lateinit var noCreatedPlaylists: TextView
    private lateinit var createdPlaylists: RecyclerView

    private lateinit var adapter: PlaylistsAdapter

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

        adapter = PlaylistsAdapter { playlist ->
            val fragment = PlaylistInfoFragment.newInstance(playlist.playlistId)
            // Открываем экран инфо
            parentFragmentManager.beginTransaction()
                .replace(R.id.rootFragmentContainerView, fragment)
                .addToBackStack(null)
                .commit()
        }
        createdPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        createdPlaylists.adapter = adapter

        newPlaylistButton.setOnClickListener {
            val fragment = CreatePlaylistFragment.newInstance()
            fragment.show(parentFragmentManager, "CreatePlaylistDialog")
        }

        // Когда вернёмся из CreatePlaylistFragment, если там создали/отредактировали,
        // придёт этот ключ => заново загружаем список
        setFragmentResultListener(CreatePlaylistFragment.PLAYLIST_CREATED_KEY) { _, _ ->
            viewModel.loadPlaylists()
        }

        // Подписка на стейт
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
