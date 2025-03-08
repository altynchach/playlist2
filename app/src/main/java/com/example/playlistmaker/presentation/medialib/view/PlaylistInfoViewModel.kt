// PlaylistInfoViewModel.kt
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
    val state: LiveData<PlaylistInfoScreenState> get() = _state

    private var currentPlaylistId: Long = 0L

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            val pl = playlistInteractor.getPlaylistById(playlistId)
            if (pl != null) {
                val trackList = fetchTracks(pl.trackIds)
                val total = calcDuration(trackList)
                _state.value = PlaylistInfoScreenState(
                    playlist = pl,
                    tracks = trackList,
                    totalDuration = total
                )
            }
        }
    }

    fun removeTrackFromPlaylist(track: Track) {
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(currentPlaylistId, track.trackId)
            loadPlaylist(currentPlaylistId)
        }
    }

    fun deletePlaylist() {
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(currentPlaylistId)
            _state.value = PlaylistInfoScreenState()
        }
    }

    fun sharePlaylist(callback: (String) -> Unit) {
        val st = _state.value ?: return
        val p = st.playlist ?: return
        if (st.tracks.isEmpty()) {
            callback("")
            return
        }
        val sb = StringBuilder()
        sb.append(p.name).append("\n")
        if (p.description.isNotEmpty()) {
            sb.append(p.description).append("\n")
        }
        val c = st.tracks.size
        sb.append("($c)\n")
        st.tracks.forEachIndexed { i, track ->
            val dt = formatTime(track.trackTime)
            sb.append("${i + 1}. ${track.artistName} - ${track.trackName} ($dt)\n")
        }
        callback(sb.toString().trim())
    }

    private suspend fun fetchTracks(trackIds: List<Long>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()
        val favList = favoritesInteractor.getFavorites().first()
        // keep order by trackIds
        return trackIds.mapNotNull { id -> favList.find { it.trackId == id } }
    }

    private fun calcDuration(tracks: List<Track>): String {
        var totalMs = 0L
        for (t in tracks) {
            totalMs += t.trackTime
        }
        val min = TimeUnit.MILLISECONDS.toMinutes(totalMs)
        return "$min ${formatMinWord(min)}"
    }

    private fun formatMinWord(min: Long): String = "минут"

    private fun formatTime(ms: Long): String {
        val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }
}
