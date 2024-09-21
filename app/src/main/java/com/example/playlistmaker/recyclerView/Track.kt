package com.example.playlistmaker.recyclerView

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("trackName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("trackTimeMillis") val trackTime: Int,
    @SerializedName("artworkUrl100") val artworkUrl100: String,
    @SerializedName("collectionName") val collectionName: String?, // Collection name can be nullable
    @SerializedName("releaseDate") val releaseDate: String, // Year of track release
    @SerializedName("primaryGenreName") val primaryGenreName: String, // Genre
    @SerializedName("country") val country: String, // Country of release
    @SerializedName("previewUrl") val previewUrl: String // Link to 30-second track preview
)