package com.example.playlistmaker.presentation.player

import androidx.lifecycle.*
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlayerViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor,
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
        viewModelScope.launch {
            val isFav = favoritesInteractor.isFavorite(track.trackId).first()
            updateState(isFavorite = isFav)
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
        updateState(isPlaying = true)
        startUpdatingProgress()
    }

    private fun pausePlayback() {
        playerInteractor.pause()
        updateState(isPlaying = false)
        stopUpdatingProgress(false)
    }

    private fun stopPlayback() {
        playerInteractor.stop()
        updateState(isPlaying = false, currentTimeFormatted = "00:00")
        stopUpdatingProgress(true)
    }

    private fun startUpdatingProgress() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                val currentState = stateLiveData.value ?: break
                if (!currentState.isPlaying) break

                val currentPositionMs = playerInteractor.getCurrentPosition()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(currentPositionMs.toLong()) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(currentPositionMs.toLong()) % 60
                val currentTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                updateState(currentTimeFormatted = currentTime)

                kotlinx.coroutines.delay(300)
            }
        }
    }

    private fun stopUpdatingProgress(isStop: Boolean) {
        updateJob?.cancel()
        updateJob = null
        if (isStop) {
            updateState(currentTimeFormatted = "00:00")
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

    fun onLikeButtonClicked() {
        val currentState = stateLiveData.value ?: return
        val track = currentState.track ?: return
        viewModelScope.launch {
            if (currentState.isFavorite) {
                favoritesInteractor.removeFavorite(track.trackId)
                updateState(isFavorite = false)
            } else {
                favoritesInteractor.addFavorite(track)
                updateState(isFavorite = true)
            }
        }
    }

    // Подгрузить плейлисты. Вызываем из onResume() в Activity
    fun loadPlaylistsForPlayerScreen(callback: (List<Playlist>) -> Unit) {
        viewModelScope.launch {
            val playlists = playlistInteractor.getAllPlaylists().first()
            callback(playlists)
        }
    }

    fun addTrackToPlaylist(
        playlistId: Long,
        trackId: Long,
        callback: (added: Boolean, playlistName: String) -> Unit
    ) {
        viewModelScope.launch {
            val playlists = playlistInteractor.getAllPlaylists().first()
            val playlist = playlists.find { it.playlistId == playlistId }
            val name = playlist?.name ?: ""
            if (playlist == null) {
                callback(false, "")
                return@launch
            }
            val result = playlistInteractor.addTrackToPlaylist(playlistId, trackId)
            callback(result, name)
        }
    }

    private fun updateState(
        isPlaying: Boolean? = null,
        currentTimeFormatted: String? = null,
        isFavorite: Boolean? = null
    ) {
        val oldState = stateLiveData.value ?: return
        val newState = oldState.copy(
            isPlaying = isPlaying ?: oldState.isPlaying,
            currentTimeFormatted = currentTimeFormatted ?: oldState.currentTimeFormatted,
            isFavorite = isFavorite ?: oldState.isFavorite
        )
        stateLiveData.value = newState
    }
}
