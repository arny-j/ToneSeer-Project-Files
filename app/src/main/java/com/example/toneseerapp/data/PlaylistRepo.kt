/**
*  This file holds the main logic for calling the Spotify API to get the user playlists and store
 *  them to the PlaylistRepository, and also to call the API to get all the Tracks for a individual
 *  selected playlist to display.
*/

package com.example.toneseerapp.data

import android.util.Log
import com.example.toneseerapp.SpotifySession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// This singleton object holds the list of playlists to use it later in other parts of the code
object PlaylistRepository {
    var playlists: List<Playlist> = emptyList() // Holds the loaded playlists
        private set
    suspend fun loadPlaylists(accessToken: String) { // Calls fetchUserPlaylist
        playlists = fetchUserPlaylists(accessToken)
    }
    fun addPlaylist(playlist: Playlist) { // create playlists and add them to the user library
        playlists = playlists + playlist
    }

    fun removePlaylist(playlistId: String) { // remove playlists
        playlists = playlists.filter { it.id != playlistId }
    }
}

// This singleton holds the playlists for the suggested songs genre selections and loads them
object GenrePlaylistRepository {
    var playlists: List<Playlist> = emptyList()
        private set

    fun addPlaylist(playlist: Playlist) {
        playlists = playlists + playlist
    }
    // This gets the playlist by genre name
    fun getPlaylistByGenre(genre: String): Playlist? {
        return playlists.find { it.name.equals(genre, ignoreCase = true) }
    }
}

// This is the call to Spotify to get all the users playlists
// Uses Coroutines to run asynchronously in the background
suspend fun fetchUserPlaylists(accessToken: String): List<Playlist> = withContext(Dispatchers.IO) { // Uses Dispatchers to run in background
    val playlists = mutableListOf<Playlist>() // To store the playlists
    var url: String? = "https://api.spotify.com/v1/me/playlists?limit=50" // The endpoint to get the playlists with a limit of 50

    // Spotify sends the data in pages, so this checks the URl for a next section, while the url is true >>>
    while (url != null) {
        val conn = URL(url).openConnection() as HttpURLConnection // Opens connection to URL
        conn.setRequestProperty("Authorization", "Bearer $accessToken") // Adds the access token to the header
        conn.requestMethod = "GET" // Set the request method to GET

        val response = conn.inputStream.bufferedReader().use { it.readText() } // Read the response into a single string
        val json = Json.parseToJsonElement(response).jsonObject // Parse the JSON string into a JSON object using Kotlinx serialization

        val itemsElement = json["items"] // This list of playlists
        val items = if (itemsElement is JsonArray) itemsElement else null // Check for the playlists and if its JSON array else null
        // Loop through each playlist and extract the data needed for the class, including the images
        if (items != null) {
            for (item in items) {
                val obj = item.jsonObject
                val id = obj["id"]!!.jsonPrimitive.content
                val name = obj["name"]!!.jsonPrimitive.content
                val description = obj["description"]?.jsonPrimitive?.contentOrNull
                val uri = obj["uri"]!!.jsonPrimitive.content

                val images = (obj["images"] as? JsonArray)?.mapNotNull { img ->
                    (img.jsonObject["url"]?.jsonPrimitive?.contentOrNull)?.let { SpotifyImage(it) }
                } ?: emptyList()
                // Create an instance of the playlist class, dump the extracted data, add the instance to the Playlists list
                playlists.add(
                    Playlist(
                        id = id,
                        name = name,
                        description = description,
                        images = images,
                        tracks = emptyList(),
                        uri = uri
                    )
                )
            }
        } else {
            Log.d("SpotifyPlaylists", "No playlists found (items is null or not an array)") // If there are no playlists
        }
        url = json["next"]?.jsonPrimitive?.contentOrNull // Checks if there is more from Spotify
    }
    playlists // Returns the list of playlists
}

// This gets the tracks for the selected playlist
suspend fun fetchPlaylistTracks(playlistId: String): List<Track> = withContext(Dispatchers.IO) {
    val url = "https://api.spotify.com/v1/playlists/$playlistId/tracks" // To connect to the API and Playlist data
    // Build request body
    val request = Request.Builder()
        .url(url)
        .addHeader("Authorization", "Bearer ${SpotifySession.accessToken}")
        .build()
    // Make the request
    OkHttpClient().newCall(request).execute().use { response ->
        if (!response.isSuccessful) { // On failed response
            Log.e("SpotifyAPI", "Failed to fetch tracks: ${response.code}")
            return@withContext emptyList()
        }
        val body = response.body?.string()
        if (body.isNullOrEmpty()) {
            Log.w("SpotifyAPI", "Response body is empty")
            return@withContext emptyList()
        }

        val tracks = parsePlaylistTracks(body)
        return@withContext tracks
    }
}

// This parses the JSON response from Spotify into Track objects then adds those objects to a list
fun parsePlaylistTracks(json: String): List<Track> {
    val result = mutableListOf<Track>() // The list of tracks
    val root = JSONObject(json)
    val items = root.getJSONArray("items")

    for (i in 0 until items.length()) {
        val item = items.getJSONObject(i).getJSONObject("track")

        val id = item.getString("id")
        val name = item.getString("name")
        val uri = item.getString("uri")
        val durationMs = item.getInt("duration_ms")

        // Artists
        val artistsJson = item.getJSONArray("artists")
        val artists = mutableListOf<Artist>()
        for (j in 0 until artistsJson.length()) {
            val artistObj = artistsJson.getJSONObject(j)
            artists.add(
                Artist(
                    name = artistObj.getString("name")
                )
            )
        }
        // Album
        val albumObj = item.getJSONObject("album")
        val albumName = albumObj.getString("name")

        val images = mutableListOf<SpotifyImage>()
        val imagesJson = albumObj.optJSONArray("images")
        if (imagesJson != null) {
            for (k in 0 until imagesJson.length()) {
                val img = imagesJson.getJSONObject(k)
                images.add(SpotifyImage(url = img.getString("url")))
            }
        }
        val album = Album(
            name = albumName,
            images = images
        )
        // Create Track object
        result.add(
            Track(
                id = id,
                name = name,
                artists = artists,
                album = album,
                durationMs = durationMs,
                uri = uri
            )
        )
    }

    return result
}

// This looks for a specified playlist in the users library, if its not there it creates it, otherwise it will get the existing playlist
// Getting user playlists for customisation will require a different functionality
suspend fun getOrCreatePlaylist(
    playlistName: String,
    accessToken: String,
    userId: String
): Playlist = withContext(Dispatchers.IO) {
    // Check repo first
    PlaylistRepository.playlists.find { it.name.equals(playlistName, ignoreCase = true) }?.let {
        return@withContext it
    }

    // Build request
    val url = "https://api.spotify.com/v1/users/$userId/playlists"
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Authorization", "Bearer $accessToken")
        setRequestProperty("Content-Type", "application/json")
        doOutput = true
    }

    // JSON body
    val body = """
        {
            "name": "$playlistName",
            "description": "Created by ToneSeer",
            "public": false
        }
    """.trimIndent()

    conn.outputStream.use { it.write(body.toByteArray()) }

    // Read response safely
    val responseCode = conn.responseCode
    val response = if (responseCode >= 400) {
        conn.errorStream?.bufferedReader()?.use { it.readText() }
            ?: throw Exception("Spotify API error $responseCode with no response body")
    } else {
        conn.inputStream.bufferedReader().use { it.readText() }
    }

    val json = Json.parseToJsonElement(response).jsonObject

    // Parse to Playlist
    val newPlaylist = Playlist(
        id = json["id"]!!.jsonPrimitive.content,
        name = json["name"]!!.jsonPrimitive.content,
        description = json["description"]?.jsonPrimitive?.contentOrNull,
        images = emptyList(),
        tracks = emptyList(),
        uri = json["uri"]!!.jsonPrimitive.content
    )

    PlaylistRepository.addPlaylist(newPlaylist)
    newPlaylist
}

