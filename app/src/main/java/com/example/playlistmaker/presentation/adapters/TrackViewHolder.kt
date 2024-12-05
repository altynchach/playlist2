package com.example.playlistmaker.presentation.adapters

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.net.HttpURLConnection
import java.net.URL

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
    private val artistNameTextView: TextView = itemView.findViewById(R.id.track_artist)
    private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)
    private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)

    fun bind(track: Track) {
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        trackTimeTextView.text = track.trackTime

        // Загрузка изображения без Picasso
        LoadImageTask(artworkImageView).execute(track.artworkUrl100)
    }

    private class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, ByteArray?>() {

        override fun doInBackground(vararg params: String?): ByteArray? {
            val imageUrl = params[0]
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                connection.inputStream.readBytes()
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: ByteArray?) {
            if (result != null) {
                val bitmap = BitmapFactory.decodeByteArray(result, 0, result.size)
                imageView.setImageBitmap(bitmap)
            } else {
                // Устанавливаем placeholder из drawable
                imageView.setImageResource(R.drawable.placeholder)
            }
        }
    }
}
