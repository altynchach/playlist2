package com.example.playlistmaker.presentation.settings

import android.content.Context
import androidx.lifecycle.*
import com.example.playlistmaker.Creator
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.presentation.states.SettingsScreenState
import com.example.playlistmaker.presentation.utils.ThemeManager

class SettingsViewModel(private val themeInteractor: ThemeInteractor) : ViewModel() {

    private val _state = MutableLiveData<SettingsScreenState>(SettingsScreenState())
    val state: LiveData<SettingsScreenState> = _state

    fun init() {
        _state.value = _state.value?.copy(
            isDarkMode = themeInteractor.isDarkMode()
        )
    }

    fun onThemeSwitchChanged(isDarkMode: Boolean) {
        themeInteractor.setDarkMode(isDarkMode)
        ThemeManager.applyTheme(isDarkMode)
        _state.value = _state.value?.copy(isDarkMode = isDarkMode)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val themeInteractor = Creator.provideThemeInteractor(context)
            return SettingsViewModel(themeInteractor) as T
        }
    }
}
