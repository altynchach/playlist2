package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.view.LikedTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class LikedTracksFragment : Fragment() {

    private val viewModel: LikedTracksViewModel by viewModel()

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
        viewModel.state.observe(viewLifecycleOwner, Observer {
        })
    }
}
