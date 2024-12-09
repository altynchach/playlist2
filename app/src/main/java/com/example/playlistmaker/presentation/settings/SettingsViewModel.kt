package com.example.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.states.SettingsScreenState

class SettingsViewModel(private val themeInteractor: ThemeInteractor) : ViewModel() {

    private val _state = MutableLiveData(SettingsScreenState())
    val state: LiveData<SettingsScreenState> = _state

    fun init() {
        _state.value = _state.value?.copy(isDarkMode = themeInteractor.isDarkMode())
    }

    fun onThemeSwitchChanged(isDarkMode: Boolean) {
        themeInteractor.setDarkMode(isDarkMode)
        _state.value = _state.value?.copy(isDarkMode = isDarkMode)
    }
}
