package com.example.playlistmaker.domain.repository

interface PlayerRepository {
    fun setDataSource(url: String)
    fun play()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
    fun setOnCompletionListener(listener: () -> Unit)
}
