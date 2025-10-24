package com.example.toneseerapp.pages

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.tv.material3.Text
import com.example.toneseerapp.components.DeviceCard
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistRepository
import com.example.toneseerapp.R
import com.example.toneseerapp.components.Sidebar
import com.example.toneseerapp.SpotifyAuthManager
import com.example.toneseerapp.SpotifySession
import com.example.toneseerapp.SpotifyTokenManager
import com.example.toneseerapp.bevan_regularFont
import com.example.toneseerapp.data.PlaylistViewModel

//@Preview(showBackground = true, device = "id:tv_1080p")
//@Composable
//fun SpotifyLoginPreview() {
//    val navController = rememberNavController()
//    ToneSeerAppTheme {
//        Surface(modifier = Modifier.fillMaxSize()) {
//            SpotifyLoginPage(navController)
//        }
//    }
//}

@Composable
fun SpotifyLoginPage(navController: NavHostController, playbackViewModel: PlaybackViewModel, playlistViewModel: PlaylistViewModel) {
    val context = LocalContext.current
    val supabaseEndpoint = "https://xxrxksifwmqvhdnumqxs.supabase.co/rest/v1/spotify_tokens"
// States
    val devices by playbackViewModel.devices.collectAsState()
    val selectedDevice by playbackViewModel.selectedDevice.collectAsState()
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tokenReady by remember { mutableStateOf(false) }

    // Start login and generate QR
    LaunchedEffect(Unit) {
        val accessToken = SpotifySession.accessToken
        val tokenValid = accessToken != null // or check expiry if you track it

        if (tokenValid) {
            tokenReady = true
            PlaylistRepository.loadPlaylists(accessToken)
            playlistViewModel.preloadGenrePlaylists()
            playbackViewModel.fetchActiveDevices(accessToken)
            return@LaunchedEffect // skip the rest of this effect
        }

        SpotifyAuthManager.startLogin(context) { bitmap ->
            qrBitmap = bitmap
        }

        val sessionId = SpotifyAuthManager.getSessionId()
        if (!sessionId.isNullOrEmpty()) {
            val success = SpotifyTokenManager.pollToken(sessionId, supabaseEndpoint)
            if (success) {
                val token = SpotifySession.accessToken
                if (!token.isNullOrEmpty()) {
                    tokenReady = true
                    PlaylistRepository.loadPlaylists(token)
                    playlistViewModel.preloadGenrePlaylists()
                    playbackViewModel.fetchActiveDevices(token)
                }
            }
        }

    }

    Box(modifier = Modifier.fillMaxSize()) {
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

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Instruction text
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xD0121212), // semi-transparent Spotify green
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (!tokenReady) "Scan the QR code with Spotify to login"
                            else "Your Logged In! Select an active device for playback",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFC0EDFF),
                            fontFamily = bevan_regularFont
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    if (!tokenReady) {
                        // QR Code UI with logo overlay
                        if (qrBitmap != null) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(300.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "Spotify Login QR",
                                    modifier = Modifier.fillMaxSize()
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.logo_qr),
                                    contentDescription = "App Logo",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .padding(4.dp)
                                )
                            }
                        } else {
                            CircularProgressIndicator(color = Color(0xFF1DB954))
                        }
                    } else {
                        // Token ready: show active devices
                        if (devices.isEmpty()) {
                            Text("No active devices found", color = Color.Gray)
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .width(400.dp)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(devices) { device ->
                                    val isSelected = selectedDevice?.id == device.id
                                    DeviceCard(
                                        device = device,
                                        isSelected = isSelected,
                                        onSelect = {
                                            playbackViewModel.selectDevice(device)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}