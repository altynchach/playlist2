package com.example.playlistmaker.presentation.medialib

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.medialib.adapter.MediatekaViewPagerAdapter
import com.example.playlistmaker.presentation.medialib.view.MediaViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaActivity : AppCompatActivity() {

    private val viewModel: MediaViewModel by viewModel()

    private lateinit var mediatekaBackButton: ImageView
    private lateinit var mediatekaTitle: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var mediatekaViewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_media)

        mediatekaBackButton = findViewById(R.id.mediatekaBackButton)
        mediatekaTitle = findViewById(R.id.mediatekaTitle)
        tabLayout = findViewById(R.id.tabLayout)
        mediatekaViewPager2 = findViewById(R.id.mediatekaViewPager2)

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
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.setSelectedTab(position)
                }
            }
        )
    }
}
