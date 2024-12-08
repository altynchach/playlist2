package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.*
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.PlayerScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerViewModel : ViewModel() {

    private val _state = MutableLiveData<PlayerScreenState>()
    val state: LiveData<PlayerScreenState> = _state

    private var mediaPlayer: MediaPlayer? = null
    private var updateJob: Job? = null

    init {
        _state.value = PlayerScreenState()
    }

    fun setTrack(track: Track) {
        _state.value = _state.value?.copy(track = track)
        preparePlayer(track.previewUrl)
    }

    private fun preparePlayer(url: String?) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        if (url == null) return

        try {
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.prepare()
            mediaPlayer?.setOnCompletionListener {
                stopPlayback()
            }
        } catch (e: IOException) {
            // Handle error if needed
        }
    }

    fun onPlayPauseClicked() {
        val curState = _state.value ?: return
        if (curState.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        mediaPlayer?.start()
        _state.value = _state.value?.copy(isPlaying = true)
        startUpdatingProgress()
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        _state.value = _state.value?.copy(isPlaying = false)
        stopUpdatingProgress()
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        _state.value = _state.value?.copy(
            isPlaying = false,
            currentTimeFormatted = "00:00"
        )
        stopUpdatingProgress()
    }

    private fun startUpdatingProgress() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                val currentPositionMs = mediaPlayer?.currentPosition ?: 0
                val minutes = (currentPositionMs / 1000) / 60
                val seconds = (currentPositionMs / 1000) % 60
                val currentTime = String.format("%02d:%02d", minutes, seconds)
                _state.value = _state.value?.copy(currentTimeFormatted = currentTime)
                delay(1000)
            }
        }
    }

    private fun stopUpdatingProgress() {
        updateJob?.cancel()
        updateJob = null
    }

    fun onPause() {
        if (_state.value?.isPlaying == true) {
            pausePlayback()
        }
    }

    fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        stopUpdatingProgress()
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlayerViewModel() as T
        }
    }
}
