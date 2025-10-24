package com.example.toneseerapp.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.toneseerapp.R
import com.example.toneseerapp.components.PlaylistsCard
import com.example.toneseerapp.components.Sidebar
import com.example.toneseerapp.components.SongSuggestionButtons
import com.example.toneseerapp.components.SuggestedSongCard
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistRepository
import com.example.toneseerapp.data.PlaylistViewModel

// This is the song suggestion page
@Composable
fun SongSuggestionPage(
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val context = LocalContext.current
    val playlists by remember { mutableStateOf(PlaylistRepository.playlists) }
    val track by playlistViewModel.selectedTrack.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background_temp),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar navigation
            Sidebar(
                onNavigateHome = { navController.navigate("home") },
                onNavigatePlaylists = { navController.navigate("playlists") },
                onNavigateSongSug = { navController.navigate("song_suggestion") },
                onNavigateSpotifyLogin = { navController.navigate("spotify_login") }
            )
            // Left column: Genre buttons + suggested song
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Genre buttons → suggest random song
                SongSuggestionButtons(playlistViewModel = playlistViewModel)

                Spacer(modifier = Modifier.size(16.dp))

                // Display the currently suggested song
                SuggestedSongCard(
                    playlistViewModel = playlistViewModel,
                    navController = navController,
                    playbackViewModel = playbackViewModel
                )
            }
            // Right column: User playlists + action buttons
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PlaylistsCard(
                    playlists = playlists,
                    playlistViewModel = playlistViewModel,
                    modifier = Modifier.weight(1f),
                    onPlaylistSelect = {
                        playlistViewModel.handlePlaylistCardClick(context, it.name)
                        playlistViewModel.suggestRandomSong(fromPlaylist = it)
                    }
                )
            }
        }
    }
}
