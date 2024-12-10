package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var isPrepared = false

    // Ensures we have a fresh MediaPlayer in Idle state
    private fun initMediaPlayer() {
        // Release any existing instance
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnErrorListener { _, what, extra ->
                onErrorListener?.invoke("MediaPlayer Error (what=$what, extra=$extra)")
                true
            }
        }
    }

    override fun setDataSource(url: String) {
        // If mediaPlayer is null, re-initialize it
        if (mediaPlayer == null) {
            initMediaPlayer()
        }

        // Now mediaPlayer should be non-null and idle
        mediaPlayer?.apply {
            reset()  // Valid to call now as the player is idle
            isPrepared = false
            try {
                setDataSource(url)
                setOnPreparedListener {
                    isPrepared = true
                }
                setOnCompletionListener {
                    onCompletionListener?.invoke()
                }
                prepareAsync()
            } catch (e: IOException) {
                onErrorListener?.invoke("Failed to set data source: ${e.message}")
            }
        }
    }

    override fun play() {
        if (mediaPlayer?.isPlaying == false && isPrepared) {
            mediaPlayer?.start()
        }
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    override fun stop() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        try {
            mediaPlayer?.prepareAsync()
            isPrepared = false
            mediaPlayer?.setOnPreparedListener {
                isPrepared = true
            }
        } catch (e: IOException) {
            onErrorListener?.invoke("Failed to prepare track after stopping: ${e.message}")
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

    fun setOnErrorListener(listener: (String) -> Unit) {
        onErrorListener = listener
    }
}