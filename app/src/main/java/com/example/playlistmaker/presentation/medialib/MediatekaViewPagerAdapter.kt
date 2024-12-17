package com.example.playlistmaker.presentation.medialib

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediatekaViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int) = when (position) {
        0 -> LikedTracksFragment.newInstance()
        else -> PlaylistsFragment.newInstance()
    }
}
