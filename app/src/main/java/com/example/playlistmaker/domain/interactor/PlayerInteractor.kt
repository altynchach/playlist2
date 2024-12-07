package com.example.playlistmaker.domain.interactor

interface PlayerInteractor {
    fun setTrackPreview(url: String)
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
    fun setOnCompletionListener(listener: () -> Unit)
}
