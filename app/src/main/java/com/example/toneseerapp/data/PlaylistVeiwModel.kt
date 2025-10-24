/**
 * This is the ViewModel for the Selected playlist.
 * It fetches the tracks from the repo and loads them to the UI elements.
 */
package com.example.toneseerapp.data

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toneseerapp.SpotifySession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.json.JSONObject


// This stores the track objects to be used in UI later
class PlaylistViewModel : ViewModel() {

// State Values, _values are only accessed within the ViewModel, the other values are for the UI and are READONLY
    // Selected Playlist
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist
    // Songs in the Selected Playlist
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks
    // Loading value, songs are ready for UI
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    // data is ready for page navigation
    private val _readyToNavigate = MutableStateFlow(false)
    val readyToNavigate: StateFlow<Boolean> = _readyToNavigate
    // Selected song in the Playlist
    private val _selectedTrack = MutableStateFlow<Track?>(null)
    val selectedTrack: StateFlow<Track?> = _selectedTrack
    // Play/Pause state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    // URL for current Tab Display
    private val _tabUrl = MutableStateFlow<String?>(null)
    val tabUrl: StateFlow<String?> = _tabUrl
    // For refreshing the Webview with the same Tab URL
    private val _tabRefresh = MutableStateFlow(0)
    val tabRefresh: StateFlow<Int> = _tabRefresh

    // Links for the suggested song playlists
    private val genrePlaylists = mapOf(
        "Rock" to "61jNo7WKLOIQkahju8i0hw",
        "Metal" to "27gN69ebwiJRtXEboL12Ih",
        "Pop" to "1WH6WVBwPBz35ZbWsgCpgr",
        "Funk" to "2mo0m9v49zuZMvES6439WU",
        "Grunge" to "7AE10X6PaavMljyunWz4gg",
        "Reggae" to "71R43lBYQZ6JQXH6LmRo1I",
        "Jazz" to "5rdgRwdMskt1IJKjNf0VWQ",
        "Country" to "5bfQIfupNOYI9rmjhUrkSh",
        "Indie" to "1PmwMQY86pJuAm7veFt3u2",
        "Punk" to "5XEDGTA0v2FGh8O2Qid8BX"
    )

    // Load all the genre playlists to the repo
    fun preloadGenrePlaylists() {
        viewModelScope.launch {
            for ((genre, id) in genrePlaylists) {
                if (GenrePlaylistRepository.getPlaylistByGenre(genre) == null) {
                    try {
                        val tracks = fetchPlaylistTracks(id)
                        val playlist = Playlist(
                            id = id,
                            name = genre,
                            images = emptyList(),
                            tracks = tracks,
                            uri = "spotify:playlist:$id"
                        )
                        GenrePlaylistRepository.addPlaylist(playlist)
                    } catch (e: Exception) {
                        Log.e("SongSuggestion", "Failed to preload genre playlist $genre", e)
                    }
                }
            }
        }
    }

    // This resets the readyToNavigate value so the homepage loads again
    fun resetReadyToNavigate() {
        _readyToNavigate.value = false
    }
    // Clears the Tab for the new URL when changed
    fun clearTabUrl() {
        _tabUrl.value = null
    }
    // For refreshing the tabs in the WebView
    fun refreshTabs() {
        _tabRefresh.value += 1
    }

    // This function creates the search URL from the selected song to display the tabs
    private fun generateSearchUrl(track: Track): String {
        val query = "${track.artists.firstOrNull()?.name ?: ""} ${track.name}" // The song
        return "https://www.songsterr.com/a/wa/search?pattern=${Uri.encode(query)}" // The endpoint
    }

    // When the user selects the track, the url is generated
    fun selectTrack(track: Track) {
        _selectedTrack.value = track
        _tabUrl.value = "https://www.songsterr.com/" // fallback homepage

        // Directly generate a Songsterr search URL
        val searchUrl = generateSearchUrl(track)
        _tabUrl.value = searchUrl
    }

    // Toggles the Play/Pause state of the song in the Spotify Playback
    fun togglePlayPause(playbackViewModel: PlaybackViewModel) {
        val track = _selectedTrack.value
        val playlist = _selectedPlaylist.value

        if (track == null || playlist == null) {
            Log.w("PlaylistViewModel", "No track or playlist selected")
            return
        }

        // if the song changes while playing
        val isNewTrack = track.uri != playbackViewModel.playbackState.value?.trackUri
        if (isNewTrack) {
            // User selected a different track → always play it
            _isPlaying.value = true
            playbackViewModel.playPlaylist(
                playlistUri = playlist.uri,
                startTrackUri = track.uri
            )
            return
        }

        // Toggle local play/pause state for the same track
        _isPlaying.value = !_isPlaying.value
        if (_isPlaying.value) {
            playbackViewModel.playPlaylist(
                playlistUri = playlist.uri,
                startTrackUri = track.uri
            )
        } else {
            playbackViewModel.pauseTrack()
        }
    }

    // This handles calling the getOrCreatePlaylist function when the main TS playlist cards are clicked
    fun handlePlaylistCardClick(context: Context, playlistName: String) {
        val accessToken = SpotifySession.accessToken
        val userId = SpotifySession.userId

        // Not logged in
        if (accessToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Log.e("PlaylistCardClick", "Access token or userId is missing")
            Toast.makeText(context, "Spotify not connected", Toast.LENGTH_SHORT).show()
            return
        }
        // Logged in, run ViewModel Functions
        viewModelScope.launch {
            try {
                _loading.value = true
                _readyToNavigate.value = false  // reset

                getOrCreatePlaylist(
                    playlistName = playlistName,
                    accessToken = accessToken,
                    userId = userId
                )
                val playlist = PlaylistRepository.playlists.find {
                    it.name.equals(playlistName, ignoreCase = true)
                } ?: throw Exception("Playlist not found in repository after creation")
                _selectedPlaylist.value = playlist

                val fetchedTracks = fetchPlaylistTracks(playlist.id)
                _tracks.value = fetchedTracks
                _readyToNavigate.value = true  // tracks are loaded, ready to navigate

            } catch (e: Exception) {
                Log.e("PlaylistCardClick", "Failed to get playlist or tracks", e)
            } finally {
                _loading.value = false
            }
        }
    }
    // For deleting a selected track from a playlist
    fun removeSelectedTrack() {
        val track = _selectedTrack.value
        val playlist = _selectedPlaylist.value

        if (track == null || playlist == null) {
            return
        }
        viewModelScope.launch {
            val accessToken = SpotifySession.accessToken
            if (accessToken.isNullOrEmpty()) return@launch

            try {
                withContext(Dispatchers.IO) {
                    val url = "https://api.spotify.com/v1/playlists/${playlist.id}/tracks"
                    val jsonBody = """
                    {
                        "tracks": [{"uri": "${track.uri}"}]
                    }
                """.trimIndent()

                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        requestMethod = "DELETE"
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }

                    conn.outputStream.use { it.write(jsonBody.toByteArray()) }

                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        // Now update local state on the main thread
                        withContext(Dispatchers.Main) {
                            _tracks.value = _tracks.value.filter { it.uri != track.uri }
                            if (_selectedTrack.value?.uri == track.uri) {
                                _selectedTrack.value = null
                            }
                        }
                    } else {
                        val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("SpotifyAPI", "Failed to remove track: $responseCode, $error")
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error removing track from Spotify", e)
            }
        }
    }

    // For adding a track to a playlist
    fun addTrackToPlaylist(track: Track, targetPlaylist: Playlist) {
        val accessToken = SpotifySession.accessToken
        if (accessToken.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            try {
                // Check if the track is already in the playlist
                val existingTracks = fetchPlaylistTracks(targetPlaylist.id)
                val alreadyInPlaylist = existingTracks.any { it.uri == track.uri }

                if (alreadyInPlaylist) {
                    Log.d("SpotifyAPI", "Track already in playlist: ${track.name}, skipping add.")
                    return@launch // Exit early
                }

                // Add track if not already present
                withContext(Dispatchers.IO) {
                    val url = "https://api.spotify.com/v1/playlists/${targetPlaylist.id}/tracks"
                    val jsonBody = """
                    {
                        "uris": ["${track.uri}"]
                    }
                """.trimIndent()

                    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }

                    conn.outputStream.use { it.write(jsonBody.toByteArray()) }

                    val responseCode = conn.responseCode
                    if (responseCode in 200..299) {
                        Log.d("SpotifyAPI", "Track added successfully: ${track.name} to ${targetPlaylist.name}")
                    } else {
                        val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("SpotifyAPI", "Failed to add track: $responseCode, $error")
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error adding track to playlist", e)
            }
        }
    }

    // Remove a playlist from the users library and the local repo
    fun deletePlaylist(playlist: Playlist) {
        val accessToken = SpotifySession.accessToken ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = "https://api.spotify.com/v1/playlists/${playlist.id}/followers"
                    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                        requestMethod = "DELETE"
                        setRequestProperty("Authorization", "Bearer $accessToken")
                    }

                    val responseCode = connection.responseCode
                    if (responseCode in 200..299) {
                        // Remove from local repository (on main thread)
                        withContext(Dispatchers.Main) {
                            PlaylistRepository.removePlaylist(playlist.id)

                            if (_selectedPlaylist.value?.id == playlist.id) {
                                _selectedPlaylist.value = null
                                _tracks.value = emptyList()
                            }
                        }
                    } else {
                        val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("SpotifyAPI", "Failed to delete playlist: $responseCode, $error")
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error deleting playlist", e)
            }
        }
    }
    // create a new playlist
    fun createPlaylist(name: String, description: String?) {
        val accessToken = SpotifySession.accessToken ?: return
        val userId = SpotifySession.userId ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val url = URL("https://api.spotify.com/v1/users/$userId/playlists")
                    val connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Authorization", "Bearer $accessToken")
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }

                    // Build JSON body
                    val body = buildString {
                        append("{\"name\":\"$name\"")
                        if (!description.isNullOrBlank()) append(",\"description\":\"$description\"")
                        append(",\"public\":false}")
                    }

                    // Send body
                    connection.outputStream.use { it.write(body.toByteArray()) }

                    val responseCode = connection.responseCode
                    if (responseCode in 200..299) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)

                        // Extract playlist details from response
                        val id = json.getString("id")
                        val playlistName = json.getString("name")
                        val playlistDesc = json.optString("description", "")
                        val playlistUri = json.getString("uri")

                        val imageList = mutableListOf<SpotifyImage>()
                        val imagesArray = json.optJSONArray("images")
                        if (imagesArray != null) {
                            for (i in 0 until imagesArray.length()) {
                                val imageObj = imagesArray.getJSONObject(i)
                                imageList.add(
                                    SpotifyImage(
                                        url = imageObj.getString("url")
                                    )
                                )
                            }
                        }
                        val newPlaylist = Playlist(
                            id = id,
                            name = playlistName,
                            description = playlistDesc,
                            images = imageList,
                            tracks = emptyList(),
                            uri = playlistUri
                        )

                        withContext(Dispatchers.Main) {
                            PlaylistRepository.addPlaylist(newPlaylist)
                        }
                    } else {
                        val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("SpotifyAPI", "Failed to create playlist: $responseCode, $error")
                    }
                }
            } catch (e: Exception) {
                Log.e("PlaylistViewModel", "Error creating playlist", e)
            }
        }
    }

    // Song Suggestion Logic
    fun suggestRandomSong(
        fromPlaylist: Playlist? = _selectedPlaylist.value,
        genre: String? = null
    ) {
        viewModelScope.launch {
            var playlistToUse: Playlist? = when {
                genre != null -> GenrePlaylistRepository.getPlaylistByGenre(genre)
                else -> fromPlaylist ?: _selectedPlaylist.value
            }
            // If the genre playlist doesn’t exist, fetch it from the API
            if (playlistToUse == null && genre != null) {
                val id = genrePlaylists[genre]
                if (id != null) {
                    try {
                        val fetchedTracks = fetchPlaylistTracks(id)
                        playlistToUse = Playlist(
                            id = id,
                            name = genre,
                            images = emptyList(),
                            tracks = fetchedTracks,
                            uri = "spotify:playlist:$id"
                        )
                        GenrePlaylistRepository.addPlaylist(playlistToUse!!) // add fetched playlist to the genrePlaylists repo
                    } catch (e: Exception) {
                        Log.e("SongSuggestion", "Failed to load genre playlist $genre", e)
                        return@launch
                    }
                }
            }
            // No playlist selected or found
            if (playlistToUse == null) {
                Log.w("SongSuggestion", "No playlist found for suggestion")
                return@launch
            }

            // Always update selected playlist and its tracks
            _selectedPlaylist.value = playlistToUse
            val tracks = if (playlistToUse.tracks.isEmpty()) {
                val fetched = fetchPlaylistTracks(playlistToUse.id)
                val updated = playlistToUse.copy(tracks = fetched)

                // update local repos
                if (genre != null) GenrePlaylistRepository.addPlaylist(updated)
                else PlaylistRepository.addPlaylist(updated)

                _selectedPlaylist.value = updated
                _tracks.value = fetched
                fetched
            } else {
                _tracks.value = playlistToUse.tracks
                playlistToUse.tracks
            }

            // Pick a random song
            if (tracks.isNotEmpty()) {
                val selected = tracks.random()
                _selectedTrack.value = selected
            } else {
                Log.w("SongSuggestion", "No tracks found for '${playlistToUse.name}'")
            }
        }
    }
    // For when a user selects a user playlist
    fun suggestAnother(playlist: Playlist) {
        // Save selected playlist
        _selectedPlaylist.value = playlist
        // Get playlist Tracks
        _tracks.value = playlist.tracks
        suggestRandomSong(playlist)
    }
}