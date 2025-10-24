/**
 * These are the main data classes, they don't have any methods, the methods are in the related
 * view models. These are to prep the data before they do into the respective lists/view models.
 */
package com.example.toneseerapp.data

// This class holds the individual playlist item
data class Playlist(
    val id: String,
    val name: String,
    val description: String? = null,
    val images: List<SpotifyImage> = emptyList(),
    val tracks: List<Track> = emptyList(),
    val uri: String
)

// This holds an individual track from a playlist, stored to a list
data class Track(
    val id: String,
    val name: String,
    val durationMs: Int,
    val artists: List<Artist> = emptyList(),
    val album: Album,
    val uri: String
)

// This holds and parses the name/s of the artist/s that created the track
data class Artist(
    val name: String
)

// This holds the album name and the image for the album
data class Album(
    val name: String,
    val images: List<SpotifyImage> = emptyList()
)

// This holds all the images that are returned for an track/playlist
data class SpotifyImage(
    val url: String
)

// This holds the data about the available devices for playback
data class SpotifyDevice(
    val id: String?,
    val is_active: Boolean,
    val is_restricted: Boolean,
    val name: String,
    val type: String,
    val volume_percent: Int?
)