package com.example.playlistmaker.presentation.trackinfo

import androidx.lifecycle.*
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AddTrackResult(
    val added: Boolean,
    val playlistName: String
)

class TrackInfoViewModel(
    private val playerInteractor: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val stateLiveData = MutableLiveData(TrackInfoScreenState())
    fun getState(): LiveData<TrackInfoScreenState> = stateLiveData

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        playerInteractor.setOnCompletionListener {
            viewModelScope.launch {
                stopPlayback()
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }

    fun setTrack(track: Track) {
        val old = stateLiveData.value ?: TrackInfoScreenState()
        val newState = old.copy(track = track)
        stateLiveData.value = newState

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
        updateState(
            isPlaying = false,
            currentTimeFormatted = "00:00"
        )
        stopUpdatingProgress(true)
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

    // Добавление трека в плейлист
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackResult {
        // Получим имя плейлиста
        val list = playlists.value ?: emptyList()
        val playlist = list.find { it.playlistId == playlistId }
        val name = playlist?.name ?: ""

        if (playlist == null) {
            return AddTrackResult(added = false, playlistName = name)
        }

        val added = playlistInteractor.addTrackToPlaylist(playlistId, track.trackId)
        return AddTrackResult(added, name)
    }

    private var progressJob: kotlinx.coroutines.Job? = null
    private fun startUpdatingProgress() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
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

    private fun stopUpdatingProgress(resetTime: Boolean) {
        progressJob?.cancel()
        progressJob = null
        if (resetTime) {
            updateState(currentTimeFormatted = "00:00")
        }
    }

    private fun updateState(
        isPlaying: Boolean? = null,
        currentTimeFormatted: String? = null,
        isFavorite: Boolean? = null
    ) {
        val old = stateLiveData.value ?: return
        val newState = old.copy(
            isPlaying = isPlaying ?: old.isPlaying,
            currentTimeFormatted = currentTimeFormatted ?: old.currentTimeFormatted,
            isFavorite = isFavorite ?: old.isFavorite
        )
        stateLiveData.value = newState
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
