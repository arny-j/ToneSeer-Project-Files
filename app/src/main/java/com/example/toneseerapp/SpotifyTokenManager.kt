package com.example.toneseerapp

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

// Singleton to hold token
object SpotifySession {
    var accessToken: String? = null
    var refreshToken: String? = null
    var expiresAt: Long? = null
    var userId: String? = null
}

// This gets the token from the DB as soon as it shows up in the DB
object SpotifyTokenManager {
    private val client = OkHttpClient()
    /*
     * Polls the serverless endpoint for the token associated with the sessionId in the Supabase DB.
     * Once received, stores it in SpotifySession singleton.
     */
    suspend fun pollToken(sessionId: String, endpointUrl: String, intervalMs: Long = 3000): Boolean {
        val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh4cnhrc2lmd21xdmhkbnVtcXhzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYxNzUyNDQsImV4cCI6MjA3MTc1MTI0NH0.KMMgFxdyhzzu5KwmjCuqlYQfagbEdr42pXB2abIZiYM"
        return withContext(Dispatchers.IO) {
            var tokenReceived = false
            while (!tokenReceived) {
                try {
                    // Query the db for the session_id and access_token
                    val request = Request.Builder()
                        .url("$endpointUrl?session_id=eq.$sessionId&access_token=not.is.null")
                        .addHeader("apikey", supabaseKey)
                        .build() // Only need the apikey for anon
                    // Execute the query
                    client.newCall(request).execute().use { response ->
                        // Handle unsuccessful response
                        if (!response.isSuccessful) {
                            Log.w("SpotifyTokenManager", "Unsuccessful response: ${response.code}")
                            // Stop here if 401 or other errors, don't try to parse body
                            delay(intervalMs)
                            return@use
                        }
                        // Read the response body
                        val body = response.body?.string()
                        if (body.isNullOrEmpty() || body == "null") {
                            // if not found log message then try again after delay
                            Log.w("SpotifyTokenManager", "No token found for session: $sessionId")
                            delay(intervalMs)
                            return@use
                        }

                        try {
                            // Parse the JSON response to the Singleton object
                            val jsonArray = org.json.JSONArray(body)
                            if (jsonArray.length() > 0) {
                                val json = jsonArray.getJSONObject(0)
                                val accessToken = json.optString("access_token", null)
                                // Store the data
                                if (!accessToken.isNullOrEmpty()) {
                                    SpotifySession.accessToken = accessToken
                                    SpotifySession.refreshToken = json.optString("refresh_token", null)
                                    // Parse the expires_at value
                                    json.optString("expires_at", null)?.takeIf { it != "null" }?.let { expiresAtStr ->
                                        try {
                                            val cleaned = expiresAtStr.replace(Regex(":(?=[0-9]{2}$)"), "")
                                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
                                            sdf.timeZone = TimeZone.getTimeZone("UTC")
                                            val date = sdf.parse(cleaned)
                                            SpotifySession.expiresAt = date?.time?.div(1000)
                                        } catch (e: Exception) {
                                            Log.w("SpotifyTokenManager", "Failed to parse expires_at: $expiresAtStr", e)
                                            SpotifySession.expiresAt = null
                                        }
                                    } ?: run { SpotifySession.expiresAt = null }
                                    try {
                                        // Fetch the userId
                                        val url = "https://api.spotify.com/v1/me"
                                        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                                        conn.requestMethod = "GET"
                                        conn.setRequestProperty("Authorization", "Bearer $accessToken")

                                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                                        val jsonUser = org.json.JSONObject(response)
                                        SpotifySession.userId = jsonUser.optString("id", null)

                                    } catch (e: Exception) {
                                        Log.w("SpotifyTokenManager", "Failed to fetch userId", e)
                                        SpotifySession.userId = null
                                    }

                                    tokenReceived = true
                                } else {
                                    Log.w("SpotifyTokenManager", "Token not ready yet")
                                    delay(intervalMs)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("SpotifyTokenManager", "Failed to parse JSON: $body", e)
                        }
                    }
                    if (!tokenReceived) delay(intervalMs)
                } catch (e: IOException) {
                    Log.w("SpotifyTokenManager", "Network error while polling token", e)
                    delay(intervalMs)
                }
            }
            tokenReceived
        }
    }
}

