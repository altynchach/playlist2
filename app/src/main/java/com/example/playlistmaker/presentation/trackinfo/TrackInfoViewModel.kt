package com.example.playlistmaker.presentation.trackinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.interactor.PlayerInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    val playlists: LiveData<List<Playlist>> get() = _playlists

    init {
        // Когда трек доигрывает
        playerInteractor.setOnCompletionListener {
            viewModelScope.launch { stopPlayback() }
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
        val oldState = stateLiveData.value ?: TrackInfoScreenState()
        val newState = oldState.copy(track = track)
        stateLiveData.value = newState

        track.previewUrl?.let {
            playerInteractor.setTrackPreview(it)
        }
        // Узнать, лайкнут ли
        viewModelScope.launch {
            val fav = favoritesInteractor.isFavorite(track.trackId).first()
            updateState(isFavorite = fav)
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

    fun onLikeButtonClicked() {
        val st = stateLiveData.value ?: return
        val track = st.track ?: return
        viewModelScope.launch {
            if (st.isFavorite) {
                favoritesInteractor.removeFavorite(track.trackId)
                updateState(isFavorite = false)
            } else {
                favoritesInteractor.addFavorite(track)
                updateState(isFavorite = true)
            }
        }
    }

    // Добавление трека
    suspend fun addTrackToPlaylist(playlistId: Long, track: Track): AddTrackResult {
        val list = playlists.value.orEmpty()
        val found = list.find { it.playlistId == playlistId } ?: return AddTrackResult(false, "")
        val name = found.name
        val added = playlistInteractor.addTrackToPlaylist(playlistId, track.trackId)
        return AddTrackResult(added, name)
    }

    // Таймер
    private var updateJob: kotlinx.coroutines.Job? = null
    private fun startUpdatingProgress() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                val st = stateLiveData.value ?: break
                if (!st.isPlaying) break

                val posMs = playerInteractor.getCurrentPosition().toLong()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(posMs) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(posMs) % 60
                val currentTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                updateState(currentTimeFormatted = currentTime)
                kotlinx.coroutines.delay(300)
            }
        }
    }

    private fun stopUpdatingProgress(reset: Boolean) {
        updateJob?.cancel()
        updateJob = null
        if (reset) {
            updateState(currentTimeFormatted = "00:00")
        }
    }

    private fun updateState(
        isPlaying: Boolean? = null,
        currentTimeFormatted: String? = null,
        isFavorite: Boolean? = null
    ) {
        val old = stateLiveData.value ?: return
        val newSt = old.copy(
            isPlaying = isPlaying ?: old.isPlaying,
            currentTimeFormatted = currentTimeFormatted ?: old.currentTimeFormatted,
            isFavorite = isFavorite ?: old.isFavorite
        )
        stateLiveData.value = newSt
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
