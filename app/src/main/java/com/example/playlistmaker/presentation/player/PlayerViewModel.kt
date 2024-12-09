package com.example.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.states.PlayerScreenState
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData(PlayerScreenState())
    fun getState(): LiveData<PlayerScreenState> = stateLiveData

    private var isUpdatingProgress = false
    private var updateThread: Thread? = null

    fun setTrack(track: Track) {
        stateLiveData.value = stateLiveData.value?.copy(track = track)
        track.previewUrl?.let { playerInteractor.setTrackPreview(it) }
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
        playerInteractor.play()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = true)
        startUpdatingProgress()
    }

    private fun pausePlayback() {
        playerInteractor.pause()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = false)
        stopUpdatingProgress()
    }

    private fun stopPlayback() {
        playerInteractor.stop()
        stateLiveData.value = stateLiveData.value?.copy(
            isPlaying = false,
            currentTimeFormatted = "00:00"
        )
        stopUpdatingProgress()
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        isUpdatingProgress = true
        val runnable = Runnable {
            while (isUpdatingProgress) {
                val currentPositionMs = playerInteractor.getCurrentPosition()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(currentPositionMs.toLong()) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(currentPositionMs.toLong()) % 60
                val currentTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                stateLiveData.postValue(stateLiveData.value?.copy(currentTimeFormatted = currentTime))
                Thread.sleep(1000)
            }
        }
        updateThread = Thread(runnable)
        updateThread?.start()
    }

    private fun stopUpdatingProgress() {
        isUpdatingProgress = false
        updateThread?.interrupt()
        updateThread = null
    }

    fun onPause() {
        if (stateLiveData.value?.isPlaying == true) {
            pausePlayback()
        }
    }

    fun onDestroy() {
        playerInteractor.release()
        stopUpdatingProgress()
    }
}
