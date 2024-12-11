package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl( private val mediaPlayer: MediaPlayer ) : PlayerRepository {

    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var isPrepared = false
    private var shouldPlay = false


    override fun setDataSource(url: String) {
        mediaPlayer.reset()
        isPrepared = false
        shouldPlay = false
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.setOnPreparedListener {
                isPrepared = true
                mediaPlayer.seekTo(0)
                if (shouldPlay) {
                    mediaPlayer.start()
                }
            }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                onErrorListener?.invoke("MediaPlayer Error (what=$what, extra=$extra)")
                true
            }
            mediaPlayer.setOnCompletionListener {
                onCompletionListener?.invoke()
            }
            mediaPlayer.prepareAsync()
        } catch (e: IOException) {
            onErrorListener?.invoke("Failed to set data source: ${e.message}")
        }
    }

    override fun play() {
        if (isPrepared) {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        } else {
            shouldPlay = true
        }
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    override fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.prepareAsync()
        isPrepared = false
        mediaPlayer.setOnPreparedListener {
            isPrepared = true
        }
    }

    override fun release() {
        mediaPlayer.reset()
        isPrepared = false
    }

    override fun isPlaying(): Boolean = mediaPlayer.isPlaying

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }
}

