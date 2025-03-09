package com.example.playlistmaker.presentation.medialib.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch

data class CreatePlaylistState(
    val isCreateButtonEnabled: Boolean = false,
    val coverFilePath: String? = null,
    val isPlaylistCreated: Boolean = false,
    // Текущее имя (для автоподстановки в поле «Название»)
    val createdPlaylistName: String = "",
    // Текущее описание (для автоподстановки в поле «Описание»)
    val createdPlaylistDesc: String = ""
)

class CreatePlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData(CreatePlaylistState())
    val state: LiveData<CreatePlaylistState> = _state

    // Храним обложку, чтобы переиспользовать при редактировании
    private var coverPath: String? = null

    /**
     * Вызывается при изменении пользователем поля «Название».
     * Если строка не пуста => кнопка активна.
     */
    fun onNameChanged(newName: String) {
        updateState(isCreateButtonEnabled = newName.isNotBlank())
    }

    /**
     * Вызывается при выборе обложки (из галереи).
     */
    fun onCoverPicked(filePath: String?) {
        coverPath = filePath
        updateState(coverFilePath = filePath)
    }

    /**
     * Создание нового плейлиста.
     */
    fun savePlaylist(name: String, description: String) {
        viewModelScope.launch {
            val pl = Playlist(
                name = name,
                description = description,
                coverFilePath = coverPath,
                trackIds = emptyList(),
                trackCount = 0
            )
            playlistInteractor.createPlaylist(pl)
            updateState(
                isPlaylistCreated = true,
                createdPlaylistName = name,
                createdPlaylistDesc = description
            )
        }
    }

    /**
     * Загрузка данных при редактировании.
     * Считываем старый плейлист и наполняем state, чтобы на экране уже было имя, описание, обложка.
     */
    fun loadPlaylistForEdit(playlistId: Long) {
        viewModelScope.launch {
            val oldPlaylist = playlistInteractor.getPlaylistById(playlistId) ?: return@launch
            coverPath = oldPlaylist.coverFilePath
            updateState(
                isCreateButtonEnabled = oldPlaylist.name.isNotBlank(),
                coverFilePath = coverPath,
                createdPlaylistName = oldPlaylist.name,
                createdPlaylistDesc = oldPlaylist.description
            )
        }
    }

    /**
     * Сохранение изменений при редактировании.
     */
    fun updatePlaylist(playlistId: Long, name: String, description: String, coverPath: String?) {
        viewModelScope.launch {
            val oldPlaylist = playlistInteractor.getPlaylistById(playlistId) ?: return@launch
            val updated = oldPlaylist.copy(
                name = name,
                description = description,
                coverFilePath = coverPath
            )
            playlistInteractor.updatePlaylist(updated)
        }
    }

    /**
     * Уточняем, есть ли уже сохранённая обложка (используется при выходе).
     */
    fun hasCover(): Boolean = coverPath != null

    // Универсальный метод для обновления стейта.
    private fun updateState(
        isCreateButtonEnabled: Boolean? = null,
        coverFilePath: String? = null,
        isPlaylistCreated: Boolean? = null,
        createdPlaylistName: String? = null,
        createdPlaylistDesc: String? = null
    ) {
        val old = _state.value ?: CreatePlaylistState()
        val newState = old.copy(
            isCreateButtonEnabled = isCreateButtonEnabled ?: old.isCreateButtonEnabled,
            coverFilePath = coverFilePath ?: old.coverFilePath,
            isPlaylistCreated = isPlaylistCreated ?: old.isPlaylistCreated,
            createdPlaylistName = createdPlaylistName ?: old.createdPlaylistName,
            createdPlaylistDesc = createdPlaylistDesc ?: old.createdPlaylistDesc
        )
        _state.value = newState
    }
}
