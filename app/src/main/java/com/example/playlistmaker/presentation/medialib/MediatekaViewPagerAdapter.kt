package com.example.playlistmaker.presentation.medialib

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MediatekaViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LikedTracksFragment.newInstance()
            1 -> PlaylistsFragment.newInstance()
            else -> LikedTracksFragment.newInstance()
        }
    }
}
