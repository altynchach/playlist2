package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LikedTracksViewModel : ViewModel() {
    private val _state = MutableLiveData<Unit>()
    val state: LiveData<Unit> = _state

    init {
        _state.value = Unit
    }
}
