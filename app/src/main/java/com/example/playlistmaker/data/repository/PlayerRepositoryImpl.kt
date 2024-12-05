package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl : PlayerRepository {

    private val mediaPlayer = MediaPlayer()
    private var completionListener: (() -> Unit)? = null

    override fun setDataSource(url: String) {
        mediaPlayer.reset()
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
        } catch (e: IOException) {
        }
    }

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        completionListener = listener
        mediaPlayer.setOnCompletionListener {
            completionListener?.invoke()
        }
    }
}
