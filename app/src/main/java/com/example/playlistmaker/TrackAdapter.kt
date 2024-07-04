package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackAdapter(private var tracks: MutableList<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount() = tracks.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.text_track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.text_artist)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.text_time)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.image_track)

        fun bind(track: Track) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName
            trackTimeTextView.text = track.trackTime

            val roundedCorners = RoundedCorners(Utils.dpToPx(8f, itemView.context))
            val imageUrl = track.artworkUrl100

            if (isInternetAvailable(itemView.context)) {
                Glide.with(itemView)
                    .load(imageUrl)
                    .transform(roundedCorners)
                    .placeholder(R.drawable.placeholder)
                    .into(artworkImageView)
            } else {
                artworkImageView.setImageResource(R.drawable.placeholder)
            }
        }

        private fun isInternetAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
