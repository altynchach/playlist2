package com.example.playlistmaker

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.recyclerView.Track
import com.example.playlistmaker.Utils.dpToPx
import com.google.gson.Gson

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val KEY_FOR_INTENT_DATA = "Selected track"
        private const val TAG = "PlayerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Инициализация UI-элементов
        val backToPrevScreenButton = findViewById<ImageView>(R.id.ivBackToPrevScr)
        val songCover = findViewById<ImageView>(R.id.ivSongCover)
        val songTitle = findViewById<TextView>(R.id.tvSongTitle)
        val artistName = findViewById<TextView>(R.id.tvAuthorOfSong)
        val trackDuration = findViewById<TextView>(R.id.tvTrackTimeChanging)
        val album = findViewById<TextView>(R.id.tvAlbumNameChanging)
        val groupOfAlbumInfo = findViewById<Group>(R.id.gAlbumInfo)
        val yearOfSoundPublished = findViewById<TextView>(R.id.tvYearOfSongChanging)
        val genreOfSong = findViewById<TextView>(R.id.tvGenreChanging)
        val countryOfSong = findViewById<TextView>(R.id.tvCountryOfSongChanging)

        // Логика возврата на предыдущий экран
        backToPrevScreenButton.setOnClickListener {
            Log.d(TAG, "Back button pressed, finishing activity")
            finish()
        }

        // Получение данных из Intent
        val json: String? = intent.getStringExtra(KEY_FOR_INTENT_DATA)
        if (json != null) {
            try {
                val currentTrack: Track = Gson().fromJson(json, Track::class.java)
                Log.d(TAG, "Received track data: $currentTrack")
                // Загрузка обложки трека
                val artworkUrl512 = currentTrack.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg")
                Glide.with(this)
                    .load(artworkUrl512)
                    .placeholder(R.drawable.placeholder)
                    .centerCrop()
                    .transform(RoundedCorners(dpToPx(8f, this)))
                    .into(songCover)

                // Присвоение данных трека UI-элементам
                songTitle.text = currentTrack.trackName ?: "-"
                artistName.text = currentTrack.artistName ?: "-"
                trackDuration.text = formatTrackDuration(currentTrack.trackTime)
                groupOfAlbumInfo.isVisible = !currentTrack.collectionName.isNullOrEmpty()
                album.text = currentTrack.collectionName ?: "-"
                yearOfSoundPublished.text = formatYear(currentTrack.releaseDate)
                genreOfSong.text = currentTrack.primaryGenreName ?: "-"
                countryOfSong.text = currentTrack.country ?: "-"
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing track data: ${e.message}")
                Toast.makeText(this, "Ошибка при загрузке данных трека", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Логирование и показ ошибки, если данные не были переданы
            Log.e(TAG, "Error: no track data received in intent")
            Toast.makeText(this, "Произошла ошибка!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val trackJson = intent.getStringExtra(KEY_FOR_INTENT_DATA)
        outState.putString(KEY_FOR_INTENT_DATA, trackJson)  // Сохраняем данные трека
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val trackJson = savedInstanceState.getString(KEY_FOR_INTENT_DATA)
        if (trackJson != null) {
            val currentTrack: Track = Gson().fromJson(trackJson, Track::class.java)
            // Повторное заполнение данных трека при восстановлении экрана
            Glide.with(this)
                .load(currentTrack.artworkUrl100?.replace("100x100bb.jpg", "512x512bb.jpg"))
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(dpToPx(8f, this)))
                .into(findViewById(R.id.ivSongCover))

            findViewById<TextView>(R.id.tvSongTitle).text = currentTrack.trackName ?: "-"
            findViewById<TextView>(R.id.tvAuthorOfSong).text = currentTrack.artistName ?: "-"
            findViewById<TextView>(R.id.tvTrackTimeChanging).text = formatTrackDuration(currentTrack.trackTime)
            findViewById<Group>(R.id.gAlbumInfo).isVisible = !currentTrack.collectionName.isNullOrEmpty()
            findViewById<TextView>(R.id.tvAlbumNameChanging).text = currentTrack.collectionName ?: "-"
            findViewById<TextView>(R.id.tvYearOfSongChanging).text = formatYear(currentTrack.releaseDate)
            findViewById<TextView>(R.id.tvGenreChanging).text = currentTrack.primaryGenreName ?: "-"
            findViewById<TextView>(R.id.tvCountryOfSongChanging).text = currentTrack.country ?: "-"
        }
    }


    // Метод для форматирования продолжительности трека
    private fun formatTrackDuration(trackTimeMillis: Int?): String {
        return if (trackTimeMillis != null) {
            val minutes = (trackTimeMillis / 1000) / 60
            val seconds = (trackTimeMillis / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } else {
            "-"
        }
    }

    // Метод для форматирования года выпуска трека
    private fun formatYear(releaseDate: String?): String {
        return releaseDate?.split("-")?.get(0) ?: "-"
    }
}
