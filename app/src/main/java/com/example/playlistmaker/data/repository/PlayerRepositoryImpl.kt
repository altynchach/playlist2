package com.example.playlistmaker.data.repository

import android.media.MediaPlayer
import com.example.playlistmaker.domain.repository.PlayerRepository
import java.io.IOException

class PlayerRepositoryImpl : PlayerRepository {

    private var mediaPlayer: MediaPlayer? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null
    private var isPrepared = false

    private fun initMediaPlayer() {

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnErrorListener { _, what, extra ->
                onErrorListener?.invoke("MediaPlayer Error (what=$what, extra=$extra)")
                true
            }
        }
    }

    override fun setDataSource(url: String) {
        if (mediaPlayer == null) {
            initMediaPlayer()
        }

        mediaPlayer?.apply {
            reset()
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