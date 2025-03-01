package com.example.playlistmaker.presentation.medialib

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Playlist

class PlaylistsAdapter(
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistsAdapter.PlaylistViewHolder>() {

    private val playlists = ArrayList<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.playlist_element, parent, false)
        return PlaylistViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    fun updateList(newList: List<Playlist>) {
        playlists.clear()
        playlists.addAll(newList)
        notifyDataSetChanged()
    }

    class PlaylistViewHolder(
        itemView: View,
        private val onClick: (Playlist) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val playlistImage: ImageView = itemView.findViewById(R.id.playlistImage)
        private val playlistName: TextView = itemView.findViewById(R.id.playlistName)
        private val numberOfTracks: TextView = itemView.findViewById(R.id.numberOfTracks)

        fun bind(playlist: Playlist) {
            playlistName.text = playlist.name
            val ctx = itemView.context
            val tracksCount = ctx.getString(R.string.playlist_tracks_count, playlist.trackCount)
            numberOfTracks.text = tracksCount

            val coverPath = playlist.coverFilePath
            if (!coverPath.isNullOrBlank()) {
                Glide.with(itemView)
                    .load(coverPath)
                    .placeholder(R.drawable.placeholder)
                    .into(playlistImage)
            } else {
                playlistImage.setImageResource(R.drawable.placeholder)
            }

            itemView.setOnClickListener {
                onClick(playlist)
            }
        }
    }
}
