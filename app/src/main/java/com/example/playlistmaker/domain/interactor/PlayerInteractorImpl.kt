package com.example.playlistmaker.domain.interactor

import com.example.playlistmaker.domain.repository.PlayerRepository

class PlayerInteractorImpl(private val playerRepository: PlayerRepository) : PlayerInteractor {

    override fun setTrackPreview(url: String) {
        playerRepository.setDataSource(url)
    }

    override fun play() {
        playerRepository.play()
    }

    override fun pause() {
        playerRepository.pause()
    }

    override fun stop() {
        playerRepository.stop()
    }

    override fun release() {
        playerRepository.release()
    }

    override fun isPlaying(): Boolean {
        return playerRepository.isPlaying()
    }

    override fun getCurrentPosition(): Int {
        return playerRepository.getCurrentPosition()
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        playerRepository.setOnCompletionListener(listener)
    }
}
