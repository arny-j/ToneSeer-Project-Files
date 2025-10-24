package com.example.toneseerapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.tv.material3.Text
import com.example.toneseerapp.R
import com.example.toneseerapp.components.PlaylistPageButtonRow
import com.example.toneseerapp.components.PlaylistsCard
import com.example.toneseerapp.components.Sidebar
import com.example.toneseerapp.components.SongsManagementCard
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistRepository
import com.example.toneseerapp.data.PlaylistViewModel

// This is the page for Playlist Management
@Composable
fun PlaylistPage(
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background_temp),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar
            Sidebar(
                onNavigateHome = { navController.navigate("home") },
                onNavigatePlaylists = { navController.navigate("playlists") },
                onNavigateSongSug = { navController.navigate("song_suggestion") },
                onNavigateSpotifyLogin = { navController.navigate("spotify_login") }
            )

            // Left column: playlists + buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Playlists grid
                val playlists by remember { mutableStateOf(PlaylistRepository.playlists) }
                PlaylistsCard(
                    playlists = playlists,
                    playlistViewModel = playlistViewModel,
                    modifier = Modifier.weight(1f),
                    onPlaylistSelect = { playlistViewModel.handlePlaylistCardClick(context, it.name) }
                )

                // Button row (Create + Delete Playlist)
                PlaylistPageButtonRow(navController = navController, playlistViewModel)
            }

            // Right column: playlist songs with management UI
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedPlaylist by playlistViewModel.selectedPlaylist.collectAsState()
                val tracks by playlistViewModel.tracks.collectAsState()

                if (selectedPlaylist != null) {
                    SongsManagementCard(
                        playlist = selectedPlaylist!!,
                        tracks = tracks,
                        playlistViewModel = playlistViewModel,
                        playbackViewModel = playbackViewModel,
                        navController = navController,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder when no playlist is selected
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, Color(0xDD121212), RoundedCornerShape(4.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xD0121212)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a playlist",
                            color = Color(0xFFC0EDFF),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}