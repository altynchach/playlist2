package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.adapter.MediatekaViewPagerAdapter
import com.example.playlistmaker.presentation.medialib.view.MediaViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class LibraryFragment : Fragment() {

    private val viewModel: MediaViewModel by viewModel()

    private lateinit var tabLayout: TabLayout
    private lateinit var mediatekaViewPager2: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tabLayout)
        mediatekaViewPager2 = view.findViewById(R.id.mediatekaViewPager2)

        val adapter = MediatekaViewPagerAdapter(requireActivity())
        mediatekaViewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, mediatekaViewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.likedTracks)
                1 -> tab.text = getString(R.string.playlists)
            }
        }.attach()

        // Observe ViewModel if needed
        viewModel.selectedTab.observe(viewLifecycleOwner) { selectedIndex ->
            mediatekaViewPager2.currentItem = selectedIndex
        }

        mediatekaViewPager2.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.setSelectedTab(position)
                }
            }
        )
    }
}
