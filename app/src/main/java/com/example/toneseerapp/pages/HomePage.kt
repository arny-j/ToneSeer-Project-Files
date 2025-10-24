package com.example.toneseerapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.tv.material3.Text
import com.example.toneseerapp.R
import com.example.toneseerapp.SpotifySession
import com.example.toneseerapp.components.MainCardList
import com.example.toneseerapp.components.Sidebar
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistViewModel

@Composable
fun HomeScreen(navController: NavHostController, playlistViewModel: PlaylistViewModel, playbackViewModel: PlaybackViewModel) {
    val readyToNavigate by playlistViewModel.readyToNavigate.collectAsState() // wait till playlist and songs are loaded
    val isLoading by playlistViewModel.loading.collectAsState()
    val isConnected = !SpotifySession.accessToken.isNullOrEmpty() // Spotify Connection
    val selectedDevice by playbackViewModel.selectedDevice.collectAsState()
    val context = LocalContext.current // for Toasts

    // Wait till the songs are loaded then navigate to the tabs page
    LaunchedEffect(readyToNavigate) {
        if (readyToNavigate) {
            navController.navigate("tabs")
            playlistViewModel.resetReadyToNavigate()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.background_temp),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(
                onNavigateHome = { navController.navigate("home") },
                onNavigatePlaylists = { navController.navigate("playlists") },
                onNavigateSongSug = { navController.navigate("song_suggestion") },
                onNavigateSpotifyLogin = { navController.navigate("spotify_login") }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.Transparent),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Connection status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xD0121212), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isConnected) "🔵 Connected to Spotify" else "❗ Not connected, Sign Into Spotify" // Spotify Connection status, changes depending on connection
                            ,
                            color = Color(0xFFC0EDFF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Selected device
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xD0121212), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedDevice?.let { "🔵 Device: ${it.name}" } ?: "No device selected",
                            color = Color(0xFFC0EDFF),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.size(36.dp))
                // The main card list showing the cards, for some reason playlist card wont navigate, no issues with playlist nav anywhere else
                MainCardList(
                    playlistViewModel = playlistViewModel,
                    onLearnCardClick = { playlistViewModel.handlePlaylistCardClick(context, "TS - Learn Something New") },
                    onOldCardClick = { playlistViewModel.handlePlaylistCardClick(context, "TS - Play Something Old") },
                    onCurrentlyLearningClick = { playlistViewModel.handlePlaylistCardClick(context, "TS - Currently Learning") },
                    onPlaylistCardClick = { navController.navigate("playlists") },
                    onSongSuggestionCardClick = { navController.navigate("song_suggestion") }
                )
            }
        }
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)), // semi-transparent overlay
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}