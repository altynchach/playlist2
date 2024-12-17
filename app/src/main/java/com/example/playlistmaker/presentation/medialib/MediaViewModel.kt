package com.example.playlistmaker.presentation.medialib

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MediaViewModel : ViewModel() {

    private val _selectedTab = MutableLiveData<Int>()
    val selectedTab: LiveData<Int> = _selectedTab

    init {
        _selectedTab.value = 0
    }

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }
}
