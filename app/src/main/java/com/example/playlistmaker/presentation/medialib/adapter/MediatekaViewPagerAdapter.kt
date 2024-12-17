package com.example.playlistmaker.presentation.medialib.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlistmaker.presentation.medialib.LikedTracksFragment
import com.example.playlistmaker.presentation.medialib.PlaylistsFragment

class MediatekaViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int) = when (position) {
        0 -> LikedTracksFragment.newInstance()
        else -> PlaylistsFragment.newInstance()
    }
}
