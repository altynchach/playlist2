package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavoritesInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

data class PlaylistInfoScreenState(
    val playlist: Playlist? = null,
    val tracks: List<Track> = emptyList(),
    val totalDuration: String = ""
)

class PlaylistInfoViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData(PlaylistInfoScreenState())
    val state: LiveData<PlaylistInfoScreenState> = _state

    private var currentPlaylistId: Long = 0L

    // Флаг, что плейлист удалён
    private val _deleted = MutableLiveData(false)
    val deleted: LiveData<Boolean> get() = _deleted

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            val pl = playlistInteractor.getPlaylistById(playlistId)
            if (pl != null) {
                playlistInteractor.getTracksForPlaylist(playlistId).collect { tracks ->
                    val dur = calculateTotalDuration(tracks)
                    _state.value = PlaylistInfoScreenState(
                        playlist = pl,
                        tracks = tracks,
                        totalDuration = dur
                    )
                }
            }
        }
    }

    fun removeTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(currentPlaylistId, track.trackId)
            refresh()
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(currentPlaylistId)
            _deleted.value = true
        }
    }

    fun sharePlaylist(callback: (String) -> Unit) {
        val st = _state.value ?: return
        val pl = st.playlist ?: return
        if (st.tracks.isEmpty()) {
            callback("")
            return
        }
        val sb = StringBuilder()
        sb.append(pl.name).append("\n")
        if (pl.description.isNotEmpty()) {
            sb.append(pl.description).append("\n")
        }
        val c = st.tracks.size
        sb.append("($c) ").append("\n")
        st.tracks.forEachIndexed { i, track ->
            val dt = formatTrackTime(track.trackTime)
            sb.append("${i + 1}. ${track.artistName} - ${track.trackName} ($dt)").append("\n")
        }
        callback(sb.toString().trim())
    }

    private fun refresh() {
        loadPlaylist(currentPlaylistId)
    }

    private fun calculateTotalDuration(tracks: List<Track>): String {
        var totalMs = 0L
        for (t in tracks) {
            totalMs += t.trackTime
        }
        val mins = TimeUnit.MILLISECONDS.toMinutes(totalMs)
        return "$mins минут"
    }

    private fun formatTrackTime(ms: Long): String {
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
