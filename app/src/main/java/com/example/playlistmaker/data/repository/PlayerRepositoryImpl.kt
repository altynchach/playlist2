package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.repository.PlayerRepository

class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null

    override fun setDataSource(url: String) {
        release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepare()
            setOnCompletionListener {
                onCompletionListener?.invoke()
            }
        }
    }

    override fun play() {
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
    }

    override fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    override fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            try {
                it.prepare()
            } catch (e: Exception) {
            }
        }
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}
