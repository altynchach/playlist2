package com.example.playlistmaker.presentation.medialib.playlistinfo

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

    fun loadPlaylist(playlistId: Long) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            val pl = playlistInteractor.getPlaylistById(playlistId)
            if (pl != null) {
                val trackIds = pl.trackIds
                val realTracks = getTracksForPlaylist(trackIds)
                val totalDur = calculateTotalDuration(realTracks)
                _state.value = PlaylistInfoScreenState(
                    playlist = pl,
                    tracks = realTracks,
                    totalDuration = totalDur
                )
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
            // Возвращаемся назад, очистив state
            _state.value = PlaylistInfoScreenState()
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
        val count = st.tracks.size
        sb.append("($count) ").append("\n")
        st.tracks.forEachIndexed { index, track ->
            val durationText = formatTrackTime(track.trackTime)
            sb.append("${index + 1}. ${track.artistName} - ${track.trackName} ($durationText)")
                .append("\n")
        }
        callback(sb.toString().trim())
    }

    private fun refresh() {
        loadPlaylist(currentPlaylistId)
    }

    private suspend fun getTracksForPlaylist(trackIds: List<Long>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()
        // Все треки из избранного
        val favList = favoritesInteractor.getFavorites().first()
        // фильтруем только те, что есть в trackIds
        val result = favList.filter { trackIds.contains(it.trackId) }
        // Сортируем по убыванию добавления: но IDs у нас в плейлисте идут с последне-добавленного (если в репо так сделано).
        // Если trackIds[0] — последний добавленный, значит надо сохранить этот порядок:
        val sorted = trackIds.mapNotNull { id -> result.find { track -> track.trackId == id } }
        return sorted
    }

    private fun calculateTotalDuration(tracks: List<Track>): String {
        var totalMs = 0L
        for (track in tracks) {
            totalMs += track.trackTime
        }
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalMs)
        return "$totalMinutes ${formatMinutesWord(totalMinutes)}"
    }

    private fun formatMinutesWord(minutes: Long): String {
        // Можно было бы учесть множественные формы
        return "минут"
    }

    private fun formatTrackTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
