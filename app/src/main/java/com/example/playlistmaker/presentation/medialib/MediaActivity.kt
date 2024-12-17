package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_media.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {

    private val viewModel: MediaViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        mediatekaBackButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val adapter = MediatekaViewPagerAdapter(this)
        mediatekaViewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, mediatekaViewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.likedTracks)
                1 -> tab.text = getString(R.string.playlists)
            }
        }.attach()

        viewModel.selectedTab.observe(this) { selectedIndex ->
            mediatekaViewPager2.currentItem = selectedIndex
        }

        mediatekaViewPager2.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.setSelectedTab(position)
                }
            }
        )
    }
}
