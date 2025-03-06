package com.example.playlistmaker.presentation.medialib.playlistinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlaylistTracksAdapter(
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<PlaylistTracksAdapter.PlaylistTrackViewHolder>() {

    private val tracks = mutableListOf<Track>()
    private var onTrackLongClick: ((Track) -> Unit)? = null

    fun setOnTrackLongClickListener(listener: (Track) -> Unit) {
        onTrackLongClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistTrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return PlaylistTrackViewHolder(view, onTrackClick, onTrackLongClick)
    }

    override fun onBindViewHolder(holder: PlaylistTrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
    }

    class PlaylistTrackViewHolder(
        itemView: View,
        private val onTrackClick: (Track) -> Unit,
        private val onTrackLongClick: ((Track) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {

        private val artworkImageView: ImageView = itemView.findViewById(R.id.track_image)
        private val trackNameTextView: TextView = itemView.findViewById(R.id.track_name)
        private val artistNameTextView: TextView = itemView.findViewById(R.id.track_artist)
        private val trackTimeTextView: TextView = itemView.findViewById(R.id.track_time)

        fun bind(track: Track) {
            trackNameTextView.text = track.trackName
            artistNameTextView.text = track.artistName

            val minutes = TimeUnit.MILLISECONDS.toMinutes(track.trackTime) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(track.trackTime) % 60
            trackTimeTextView.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .centerCrop()
                .transform(RoundedCorners(2))
                .placeholder(R.drawable.placeholder)
                .into(artworkImageView)

            itemView.setOnClickListener {
                onTrackClick(track)
            }
            itemView.setOnLongClickListener {
                onTrackLongClick?.invoke(track)
                true
            }
        }
    }
}
