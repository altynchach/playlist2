package com.example.playlistmaker.recyclerView

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R

class TrackAdapter(private var tracks: ArrayList<Track>) : RecyclerView.Adapter<TrackViewHolder>() {

    private var onTrackClickListener: ((Track) -> Unit)? = null

    companion object {
        private const val TAG = "TrackAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            Log.d(TAG, "Track clicked: ${track.trackName} by ${track.artistName}")
            onTrackClickListener?.invoke(track)
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        notifyDataSetChanged()
        Log.d(TAG, "Tracks updated: ${newTracks.size} items")
    }

    fun setOnTrackClickListener(listener: (Track) -> Unit) {
        onTrackClickListener = listener
    }
}
