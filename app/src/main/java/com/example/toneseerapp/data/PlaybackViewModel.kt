/**
 * This is the backend for all the Playback functionality. Handles the device selection, and the playback
 * calls to Spotify.
 */
package com.example.toneseerapp.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toneseerapp.SpotifySession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

// This is the backend for all the playback functionality
class PlaybackViewModel : ViewModel() {
// State Values, _values are only accessed within the ViewModel, the other values are for the UI and are READONLY
    // User Devices
    private val _devices = MutableStateFlow<List<SpotifyDevice>>(emptyList())
    val devices: StateFlow<List<SpotifyDevice>> = _devices
    // Users selected device
    private val _selectedDevice = MutableStateFlow<SpotifyDevice?>(null)
    val selectedDevice: StateFlow<SpotifyDevice?> = _selectedDevice
    // The current playback state
    private val _playbackState = MutableStateFlow<PlaybackStatus?>(null)
    val playbackState: StateFlow<PlaybackStatus?> = _playbackState
    // Song repeat status
    private val _isRepeatingTrack = MutableStateFlow(false)
    val isRepeatingTrack: StateFlow<Boolean> = _isRepeatingTrack
    // Playlist shuffle status
    private val _isShuffling = MutableStateFlow(false)
    val isShuffling: StateFlow<Boolean> = _isShuffling

    // Playback info
    private var lastTrackUri: String? = null
    private var lastPositionMs: Int = 0
    private var isPaused: Boolean = false

    // For polling the Spotify API for current playback state
    private var pollingJob: Job? = null

    // Get all the available devices
    fun fetchActiveDevices(accessToken: String) {
        viewModelScope.launch {
            val devices = withContext(Dispatchers.IO) { getAvailableDevices(accessToken) }
            _devices.value = devices
        }
    }

    // Select a device for playback
    fun selectDevice(device: SpotifyDevice) {
        _selectedDevice.value = device
        startPollingPlayback()
    }

    // Poll for playback information from Spotify (playtime, status)
    private fun startPollingPlayback() {
        pollingJob?.cancel()
        val accessToken = SpotifySession.accessToken
        if (accessToken.isNullOrEmpty()) return
        // Polling job launched asynchronously
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            while (true) {
                try {
                    val deviceId = _selectedDevice.value?.id
                    if (!deviceId.isNullOrEmpty()) {
                        val request = Request.Builder()
                            .url("https://api.spotify.com/v1/me/player")
                            .addHeader("Authorization", "Bearer $accessToken")
                            .build()
                        // Send request and Parse JSON response from API
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val jsonStr = response.body?.string()
                            val json = jsonStr?.let { JSONObject(it) }

                            // Update playback state
                            val item = json?.optJSONObject("item")
                            val trackUri = item?.optString("uri") ?: lastTrackUri
                            val progressMs = json?.optInt("progress_ms") ?: lastPositionMs
                            val paused = json?.optBoolean("is_playing")?.not() ?: true

                            // Update repeat state
                            val repeatState = json?.optString("repeat_state") ?: "off"
                            val repeating = repeatState == "track"
                            if (_isRepeatingTrack.value != repeating) {
                                _isRepeatingTrack.value = repeating
                            }

                            // Update shuffle state if changed
                            val shuffleState = json?.optBoolean("shuffle_state") ?: false
                            if (_isShuffling.value != shuffleState) {
                                _isShuffling.value = shuffleState
                            }

                            // Update state if changed
                            if (trackUri != lastTrackUri || progressMs != lastPositionMs || paused != isPaused) {
                                lastTrackUri = trackUri
                                lastPositionMs = progressMs
                                isPaused = paused
                                _playbackState.value = PlaybackStatus(trackUri, progressMs, paused)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("PlaybackPolling", "Error polling playback", e)
                }
                delay(1000) // poll every second
            }
        }
    }

    // This will call the API to get the track data for the current playing track if the track does not already existed in the selected playlist object
    suspend fun getTrackInfo(trackUri: String): Track = withContext(Dispatchers.IO) {
        val accessToken = SpotifySession.accessToken
        val trackId = trackUri.substringAfterLast(":") // get the Spotify track ID
        val url = "https://api.spotify.com/v1/tracks/$trackId"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        OkHttpClient().newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to fetch track info: ${response.code}")

            val json = JSONObject(response.body?.string() ?: "{}")
            val name = json.getString("name")
            val durationMs = json.getInt("duration_ms")

            val artists = json.getJSONArray("artists").let { array ->
                List(array.length()) { i ->
                    Artist(array.getJSONObject(i).getString("name"))
                }
            }

            val albumObj = json.getJSONObject("album")
            val albumName = albumObj.getString("name")
            val images = albumObj.optJSONArray("images")?.let { array ->
                List(array.length()) { i ->
                    SpotifyImage(array.getJSONObject(i).getString("url"))
                }
            } ?: emptyList()

            Track(
                id = trackId,
                name = name,
                durationMs = durationMs,
                artists = artists,
                album = Album(albumName, images),
                uri = trackUri
            )
        }
    }

    // Stops polling
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    // Plays the selected song inside the selected playlist so when the track ends, the next song in the queue will play
    fun playPlaylist(playlistUri: String, startTrackUri: String? = null) {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = if (startTrackUri != null) {
                    // If same track is paused, resume from last position
                    if (startTrackUri == lastTrackUri && isPaused) {
                        """
                    {
                      "context_uri": "$playlistUri",
                      "offset": { "uri": "$startTrackUri" },
                      "position_ms": $lastPositionMs
                    }
                    """
                    } else {
                        // New track, start from beginning
                        lastPositionMs = 0
                        """
                    {
                      "context_uri": "$playlistUri",
                      "offset": { "uri": "$startTrackUri" }
                    }
                    """
                    }
                } else {
                    """{ "context_uri": "$playlistUri" }"""
                }

                val requestBody = RequestBody.create("application/json".toMediaType(), json)

                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/play?device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    isPaused = false
                    _playbackState.value = PlaybackStatus(
                        trackUri = startTrackUri,
                        progressMs = 0,
                        isPaused = false
                    )
                } else {
                    Log.e("PlaybackViewModel", "Failed to start playlist: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error playing playlist", e)
            }
        }
    }

    // Pauses playback
    fun pauseTrack() {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                // Use last known position from polling
                val pausePosition = lastPositionMs

                // Send pause command
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/pause?device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(RequestBody.create(null, ByteArray(0)))
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    isPaused = true
                    _playbackState.value = PlaybackStatus(lastTrackUri, pausePosition, true)
                } else {
                    Log.e("PlaybackViewModel", "Failed to pause playback: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error pausing track", e)
            }
        }
    }

    // Skip to next track
    fun skipNext() {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/next?device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(RequestBody.create(null, ByteArray(0)))
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                } else {
                    Log.e("PlaybackViewModel", "Failed to skip next: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error skipping next track", e)
            }
        }
    }

    // Skip to previous track
    fun skipPrevious() {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/previous?device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(RequestBody.create(null, ByteArray(0)))
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                } else {
                    Log.e("PlaybackViewModel", "Failed to skip previous: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error skipping previous track", e)
            }
        }
    }

    // Restart current track
    fun restartTrack(playlistViewModel: PlaylistViewModel) {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        val trackUri = lastTrackUri
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty() || trackUri.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                // Seek to 0ms
                val seekRequest = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/seek?position_ms=0&device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(RequestBody.create(null, ByteArray(0)))
                    .build()

                client.newCall(seekRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e("PlaybackViewModel", "Failed to seek track: ${response.code}")
                        return@launch
                    }
                }

                lastPositionMs = 0

                // Pause playback
                val pauseRequest = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/pause?device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(RequestBody.create(null, ByteArray(0)))
                    .build()

                client.newCall(pauseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        isPaused = true
                        _playbackState.value = PlaybackStatus(trackUri, 0, true)
                        playlistViewModel.refreshTabs()
                    } else {
                        Log.e("PlaybackViewModel", "Failed to pause track after seek: ${response.code}")
                    }
                }

            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error restarting track", e)
            }
        }
    }
    // Toggle song repeat
    fun toggleRepeatTrack() {
        val deviceId = selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newState = if (_isRepeatingTrack.value) "off" else "track"
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/repeat?state=$newState&device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(RequestBody.create(null, ByteArray(0))) // empty body
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                    _isRepeatingTrack.value = !_isRepeatingTrack.value
                } else {
                    Log.e("PlaybackViewModel", "Failed to toggle repeat: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error toggling repeat", e)
            }
        }
    }

    // Toggle the Playlist Shuffle
    fun toggleShuffle() {
        val deviceId = _selectedDevice.value?.id
        val accessToken = SpotifySession.accessToken
        if (deviceId.isNullOrEmpty() || accessToken.isNullOrEmpty()) return

        val newShuffleState = !_isShuffling.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.spotify.com/v1/me/player/shuffle?state=$newShuffleState&device_id=$deviceId")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .put(RequestBody.create(null, ByteArray(0)))
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                if (response.isSuccessful) {
                    _isShuffling.value = newShuffleState
                } else {
                    Log.e("PlaybackViewModel", "Failed to toggle shuffle: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("PlaybackViewModel", "Error toggling shuffle", e)
            }
        }
    }
}

// Store the status of playback from Spotify
data class PlaybackStatus(
    val trackUri: String?,
    val progressMs: Int,
    val isPaused: Boolean
)

// Call Spotify to get the available devices
suspend fun getAvailableDevices(accessToken: String): List<SpotifyDevice> = withContext(Dispatchers.IO) {
    val url = "https://api.spotify.com/v1/me/player/devices"

    val request = Request.Builder()
        .url(url)
        .get()
        .addHeader("Authorization", "Bearer $accessToken")
        .build()

    val client = OkHttpClient()

    try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.e("SpotifyDevices", "Failed to get devices: ${response.code}")
            return@withContext emptyList()
        }

        val body = response.body?.string()
        val json = JSONObject(body ?: "{}")
        val devicesJson = json.getJSONArray("devices")

        val devices = mutableListOf<SpotifyDevice>()
        for (i in 0 until devicesJson.length()) {
            val d = devicesJson.getJSONObject(i)
            val isActive = d.getBoolean("is_active")
            val device = SpotifyDevice(
                id = d.optString("id", null),
                is_active = isActive,
                is_restricted = d.getBoolean("is_restricted"),
                name = d.getString("name"),
                type = d.getString("type"),
                volume_percent = d.optInt("volume_percent", -1).takeIf { it >= 0 }
            )
            devices.add(device)
        }
        devices
    } catch (e: Exception) {
        Log.e("SpotifyDevices", "Error fetching devices", e)
        emptyList()
    }
}