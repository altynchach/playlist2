package com.example.playlistmaker.presentation.medialib.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
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

    override fun getItemCount(): Int = playlists.size

    fun updateList(newList: List<Playlist>) {
        val diffCallback = PlaylistDiffCallback(playlists, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        playlists.clear()
        playlists.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
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

            val tracksCount = ctx.resources.getQuantityString(
                R.plurals.playlist_tracks_count,
                playlist.trackCount,
                playlist.trackCount
            )
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

    private class PlaylistDiffCallback(
        val oldList: List<Playlist>,
        val newList: List<Playlist>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPlaylist = oldList[oldItemPosition]
            val newPlaylist = newList[newItemPosition]
            return oldPlaylist.playlistId == newPlaylist.playlistId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val o = oldList[oldItemPosition]
            val n = newList[newItemPosition]
            return o.name == n.name &&
                    o.trackCount == n.trackCount &&
                    o.coverFilePath == n.coverFilePath
        }
    }
}
