package com.example.playlistmaker
package com.example.playlistmaker.recyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class TrackAdapter : RecyclerView.Adapter<TrackViewHolder>() {
    var tracks = ArrayList<Track>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }
    override fun getItemCount(): Int = tracks.size
}