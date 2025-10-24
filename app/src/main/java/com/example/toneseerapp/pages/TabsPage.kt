package com.example.toneseerapp.pages

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.toneseerapp.data.Album
import com.example.toneseerapp.data.Artist
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistViewModel
import com.example.toneseerapp.R
import com.example.toneseerapp.components.RefreshTabsButton
import com.example.toneseerapp.components.SelectedTrackCard
import com.example.toneseerapp.components.Sidebar
import com.example.toneseerapp.components.SongsDisplayCard
import com.example.toneseerapp.components.TabPageButtonRow
import com.example.toneseerapp.components.TabsCard
import com.example.toneseerapp.data.Track
import com.example.toneseerapp.toggleTabsPlayPause
import com.example.toneseerapp.ui.theme.ToneSeerAppTheme

// Tabs Page
@Composable
fun TabsPage(
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel,
    playbackViewModel: PlaybackViewModel)
{
    // Playlist and Track information
    val selectedPlaylist by playlistViewModel.selectedPlaylist.collectAsState()
    val tracks by playlistViewModel.tracks.collectAsState()
    val selectedTrack by playlistViewModel.selectedTrack.collectAsState()

    // This loads the selected track to the UI
//    LaunchedEffect(selectedTrack) {
//        selectedTrack?.let {
//            Log.d("TabsPage", "Selected track: ${it.name}")
//        }
//    }

    // Playback States and flags
    val isLoading by playlistViewModel.loading.collectAsState()
    val isRepeating by playbackViewModel.isRepeatingTrack.collectAsState()
    val isShuffling by playbackViewModel.isShuffling.collectAsState()
    val spotifyPlayback by playbackViewModel.playbackState.collectAsState() // playback state
    val isSpotifyPlaying = spotifyPlayback?.isPaused?.not() ?: false

    // The current URL for the selected Tab
    val currentTrackUri = spotifyPlayback?.trackUri
    val webViewRef = remember { mutableStateOf<WebView?>(null) }


    // Flag to request focus for the selected track's play/pause button
    var moveFocusToButton by remember { mutableStateOf(false) }

    // This will set the selectedTrack item in the UI to be whatever is currently playing in the users Spotify
    LaunchedEffect(currentTrackUri) {
        val currentUri = currentTrackUri
        if (currentUri != null && currentUri != selectedTrack?.uri) {
            val trackInPlaylist = tracks.find { it.uri == currentUri }
            val trackToSelect = trackInPlaylist ?: playbackViewModel.getTrackInfo(currentUri)
            playlistViewModel.selectTrack(trackToSelect)
            moveFocusToButton = true
        } else {
            Log.d("TabsPage", "Keeping previously selected track: ${selectedTrack?.name}")
        }
    }

    // In case the tracks take too long to load
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Main Content
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background_temp),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Sidebar
        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(
                onNavigateHome = { navController.popBackStack(route = "home", inclusive = false) },
                onNavigatePlaylists = { navController.navigate("playlists") },
                onNavigateSongSug = { navController.navigate("song_suggestion") },
                onNavigateSpotifyLogin = { navController.navigate("spotify_login") }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Show the playlist tracks
                            if (selectedPlaylist != null) {
                                SongsDisplayCard(
                                    playlist = selectedPlaylist!!,
                                    tracks = tracks,
                                    playlistViewModel = playlistViewModel,
                                    modifier = Modifier,
                                    playbackViewModel = playbackViewModel,
                                    onTrackSelect = { track ->
                                        playlistViewModel.selectTrack(track)
                                        moveFocusToButton = true
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .width(250.dp)
                                        .height(400.dp)
                                        .border(2.dp, Color(0xDD121212), RoundedCornerShape(4.dp))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xD0121212)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Select a playlist", color = Color(0xFFC0EDFF))
                                }
                            }
                            // This holds the WebView and the Tabs Display
                            TabsCard(
                                title = "Tablatures",
                                modifier = Modifier,
                                playlistViewModel = playlistViewModel,
                                webViewRef = webViewRef
                            )
                        }
                    }
                }
                // Selected Song Card + Buttons Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectedTrackCard(
                        // No selected Track:
                        track = selectedTrack ?: Track(
                            id = "placeholder",
                            name = "No track selected",
                            durationMs = 0,
                            artists = listOf(Artist("")),
                            album = Album("No Album"),
                            uri = ""
                        ),
                        playlistViewModel = playlistViewModel,
                        // Playback States
                        isPlaying = selectedTrack?.uri == currentTrackUri && isSpotifyPlaying,
                        isRepeating = isRepeating,
                        isShuffling = isShuffling,
                        // Playback Functions
                        onPlayPauseClick = { playlistViewModel.togglePlayPause(playbackViewModel) },
                        onRestartClick = { playbackViewModel.restartTrack(playlistViewModel) },
                        onPreviousClick = { playbackViewModel.skipPrevious() },
                        onNextClick = { playbackViewModel.skipNext() },
                        onToggleRepeatClick = { playbackViewModel.toggleRepeatTrack() },
                        onToggleShuffleClick = { playbackViewModel.toggleShuffle() },
                        // Move Focus
                        moveFocusToButton = moveFocusToButton,
                        modifier = Modifier.weight(1f)
                    )

                    // Button row next to the card
                    TabPageButtonRow(
                        modifier = Modifier.width(100.dp),
                        selectedTrack = selectedTrack,
                        playlistViewModel = playlistViewModel
                    )
                }
                // Reset the focus flag after requesting focus
                LaunchedEffect(moveFocusToButton) {
                    if (moveFocusToButton) moveFocusToButton = false
                }
            }
        }
    }
}