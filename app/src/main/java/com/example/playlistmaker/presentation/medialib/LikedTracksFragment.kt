package com.example.playlistmaker.presentation.medialib

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.medialib.view.LikedTracksScreenState
import com.example.playlistmaker.presentation.medialib.view.LikedTracksViewModel
import com.example.playlistmaker.presentation.player.PlayerActivity
import com.example.playlistmaker.presentation.search.adapters.TrackAdapter
import com.google.gson.Gson
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LikedTracksFragment : Fragment() {

    private val viewModel: LikedTracksViewModel by viewModel()

    private lateinit var adapter: TrackAdapter
    private lateinit var likedTracksRecycler: RecyclerView
    private lateinit var mediatekaIsEmpty: ImageView
    private lateinit var mediatekaIsEmptyText: TextView

    companion object {
        fun newInstance(): LikedTracksFragment {
            return LikedTracksFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_liked_tracks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        likedTracksRecycler = view.findViewById(R.id.liked_tracks_recycler)
        mediatekaIsEmpty = view.findViewById(R.id.mediatekaIsEmpty)
        mediatekaIsEmptyText = view.findViewById(R.id.mediatekaIsEmptyText)

        adapter = TrackAdapter(arrayListOf())
        likedTracksRecycler.layoutManager = LinearLayoutManager(requireContext())
        likedTracksRecycler.adapter = adapter

        adapter.setOnTrackClickListener { track ->
            openPlayerActivity(track)
        }

        viewModel.state.observe(viewLifecycleOwner, Observer { state ->
            renderState(state)
        })
    }

    private fun renderState(state: LikedTracksScreenState) {
        if (state.isEmpty) {
            mediatekaIsEmpty.visibility = View.VISIBLE
            mediatekaIsEmptyText.visibility = View.VISIBLE
            likedTracksRecycler.visibility = View.GONE
        } else {
            mediatekaIsEmpty.visibility = View.GONE
            mediatekaIsEmptyText.visibility = View.GONE
            likedTracksRecycler.visibility = View.VISIBLE
            adapter.updateTracks(state.tracks)
        }
    }

    private fun openPlayerActivity(track: Track) {
        val ctx = context ?: return
        val displayIntent = Intent(ctx, PlayerActivity::class.java)
        val strTrack = Gson().toJson(track)
        displayIntent.putExtra(PlayerActivity.NAME_TRACK, strTrack)
        startActivity(displayIntent)
    }
}
