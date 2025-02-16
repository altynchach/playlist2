package com.example.playlistmaker.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData(PlayerScreenState())
    fun getState(): LiveData<PlayerScreenState> = stateLiveData

    private var updateJob: Job? = null

    init {
        playerInteractor.setOnCompletionListener {
            viewModelScope.launch {
                stopPlayback()
            }
        }
    }

    fun setTrack(track: Track) {
        stateLiveData.value = stateLiveData.value?.copy(track = track)
        track.previewUrl?.let {
            playerInteractor.setTrackPreview(it)
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
        playerInteractor.play()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = true)
        startUpdatingProgress()
    }

    private fun pausePlayback() {
        playerInteractor.pause()
        stateLiveData.value = stateLiveData.value?.copy(isPlaying = false)
        stopUpdatingProgress(isStop = false)
    }

    private fun stopPlayback() {
        playerInteractor.stop()
        stateLiveData.value = stateLiveData.value?.copy(
            isPlaying = false,
            currentTimeFormatted = "00:00"
        )
        stopUpdatingProgress(isStop = true)
    }

    private fun startUpdatingProgress() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isActive && stateLiveData.value?.isPlaying == true) {
                val currentPositionMs = playerInteractor.getCurrentPosition()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(currentPositionMs.toLong()) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(currentPositionMs.toLong()) % 60
                val currentTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                val oldState = stateLiveData.value
                if (oldState != null) {
                    stateLiveData.value = oldState.copy(currentTimeFormatted = currentTime)
                }

                delay(300) // обновляем прогресс каждые 300 мс
            }
        }
    }

    private fun stopUpdatingProgress(isStop: Boolean) {
        updateJob?.cancel()
        updateJob = null
        if (isStop) {
            val oldState = stateLiveData.value
            if (oldState != null) {
                stateLiveData.value = oldState.copy(currentTimeFormatted = "00:00")
            }
        }
    }

    fun onPause() {
        if (stateLiveData.value?.isPlaying == true) {
            pausePlayback()
        }
    }

    fun onDestroy() {
        stopUpdatingProgress(true)
        playerInteractor.release()
    }
}
