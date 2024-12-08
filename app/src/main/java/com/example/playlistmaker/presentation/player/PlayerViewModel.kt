package com.example.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.PlayerScreenState
import java.io.IOException

class PlayerViewModel : ViewModel() {

    private val stateLiveData = MutableLiveData(PlayerScreenState())
    fun getState(): LiveData<PlayerScreenState> = stateLiveData

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    fun setTrack(track: Track) {
        stateLiveData.value = stateLiveData.value?.copy(track = track)
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
        }
    }

    fun onPlayPauseClicked() {
        val currentState = stateLiveData.value ?: return
        if (currentState.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        mediaPlayer?.start()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = true)
        startUpdatingProgress()
    }

    private fun pausePlayback() {
        mediaPlayer?.pause()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = false)
        stopUpdatingProgress()
    }

    private fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        stateLiveData.value = stateLiveData.value?.copy(
            isPlaying = false,
            currentTimeFormatted = "00:00"
        )
        stopUpdatingProgress()
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateRunnable = object : Runnable {
            override fun run() {
                val currentPositionMs = mediaPlayer?.currentPosition ?: 0
                val minutes = (currentPositionMs / 1000) / 60
                val seconds = (currentPositionMs / 1000) % 60
                val currentTime = String.format("%02d:%02d", minutes, seconds)
                stateLiveData.value = stateLiveData.value?.copy(currentTimeFormatted = currentTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun stopUpdatingProgress() {
        updateRunnable?.let { handler.removeCallbacks(it) }
        updateRunnable = null
    }

    fun onPause() {
        if (stateLiveData.value?.isPlaying == true) {
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
