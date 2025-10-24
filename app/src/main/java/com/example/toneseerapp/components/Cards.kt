package com.example.toneseerapp.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.toneseerapp.R
import com.example.toneseerapp.bevan_italicFont
import com.example.toneseerapp.bevan_regularFont
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.Playlist
import com.example.toneseerapp.data.PlaylistRepository
import com.example.toneseerapp.data.PlaylistViewModel
import com.example.toneseerapp.data.SpotifyDevice
import com.example.toneseerapp.data.Track
import com.example.toneseerapp.jomhuriaFont
import com.example.toneseerapp.toggleTabsPlayPause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


// This is the big row in the homepage that has the Main Playlist cards
@Composable
fun MainCardList(
    playlistViewModel: PlaylistViewModel,
    onLearnCardClick: () -> Unit,
    onOldCardClick: () -> Unit,
    onPlaylistCardClick: () -> Unit,
    onCurrentlyLearningClick: () -> Unit,
    onSongSuggestionCardClick: () -> Unit
) {
    val cardTitles = listOf("Learn Something New", "Currently Learning", "Play Something Old", "User Playlists", "Song Suggestion")
    // This is the scrollable row of cards
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.4f))
            .height(320.dp)
            .padding(10.dp)
            .border(2.dp, Color.Transparent, RoundedCornerShape(8.dp)),
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        items(cardTitles.size) { index ->
            val title = cardTitles[index]
            // Actions when the cards are clicked
            val clickHandler: () -> Unit = when (title) {
                "Learn Something New" -> { { onLearnCardClick() } }
                "Currently Learning" -> { { onCurrentlyLearningClick() } }
                "Play Something Old" -> { { onOldCardClick() } }
                "Your Playlists" -> { { onPlaylistCardClick() } }
                "Song Suggestion" -> { { onSongSuggestionCardClick() } }
                else -> { { } }
            }
            // The cards
            MainCardItem(
                title = title,
                onClick = clickHandler
            )
        }
    }
}

// The card element that is displayed in the MainCardList
@Composable
fun MainCardItem(title: String, onClick: (() -> Unit)? = null) {
    var isFocused by remember { mutableStateOf(false) } // Focus state
    val imageRes = when (title) { // images for the cards
        "Learn Something New" -> R.drawable.learn_new
        "Play Something Old" -> R.drawable.play_something_old
        "Currently Learning" -> R.drawable.learning
        "User Playlists" -> R.drawable.playlist_management_card
        "Song Suggestion" -> R.drawable.songsuggestion_card
        else -> null
    }
    val borderColor by animateColorAsState( // Border Colour animation
        targetValue = if(isFocused) Color(0xFF1DA54F) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "CardItemBorderColor"
    )

    Box(
        modifier = Modifier
            .width(500.dp)
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .size(width = 500.dp, height = 300.dp)
                .onFocusChanged { isFocused = it.isFocused }
                .let {
                    if (onClick != null) it.clickable { onClick() } else it
                }


        ) {
            imageRes?.let { // set the image
                Image(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // text for the card
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0xAD2E3A4F).copy(alpha = 0.4f))
                    .border(1.dp, Color.Transparent, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(44.dp)),
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFC0EDFF),
                fontFamily = bevan_regularFont
            )
        }
    }
}

// This holds and displays the individual tracks in the selected playlist
@Composable
fun SongsDisplayCard(
    playlist: Playlist,
    tracks: List<Track>,
    playlistViewModel: PlaylistViewModel,
    playbackViewModel: PlaybackViewModel,
    modifier: Modifier = Modifier,
    onTrackSelect: ((Track) -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) } // focus state
    val spotifyPlayback by playbackViewModel.playbackState.collectAsState() // playback state
    val isSpotifyPlaying = spotifyPlayback?.isPaused?.not() ?: false
    val currentTrackUri = spotifyPlayback?.trackUri // current song uri
    val borderColor by animateColorAsState( // border color
        targetValue = if (isFocused) Color(0xFF1DA54F) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "SpotifyCardBorderColor"
    )

    Box(
        modifier = modifier
            .width(230.dp)
            .height(420.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xD0121212))
            .padding(4.dp)
    ) {
        if (tracks.isNotEmpty()) {
            // Scrollable list of tracks filling the available height
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(2.dp)
            ) {
                items(tracks) { track ->
                    val isTrackPlaying = track.uri == currentTrackUri && isSpotifyPlaying
                    // The track card displaying individual songs
                    TrackCard(
                        track = track,
                        playlistViewModel = playlistViewModel,
                        onPlayPauseClick = {
                            if (isTrackPlaying) {
                                playbackViewModel.pauseTrack()
                            } else {
                                playbackViewModel.playPlaylist(track.uri)
                            }
                        },
                        onSelect = {
                            playlistViewModel.selectTrack(track)
                            onTrackSelect?.invoke(track)
                        }
                    )
                }
            }
        } else {
            // Placeholder if there are no tracks yet
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(400.dp)
                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks available",
                    color = Color(0xFFC0EDFF),
                    fontSize = 16.sp,
                    fontFamily = bevan_regularFont
                )
            }
        }
    }
}

// This displays the track data for each track on the tabs page
@Composable
fun TrackCard(
    track: Track,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel,
    onPlayPauseClick: () -> Unit = {},
    onSelect: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) } // focus state
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF1DA54F) else Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "TrackCardBorderColor"
    )

    // Load album image as Bitmap asynchronously
    var albumBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageUrl = track.album.images.firstOrNull()?.url
    val imageCache = remember { mutableMapOf<String, Bitmap>() }

    // Load image
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrEmpty()) {
            albumBitmap = imageCache[imageUrl] ?: try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(java.net.URL(imageUrl).openStream())
                }
                imageCache[imageUrl] = bitmap
                bitmap
            } catch (e: Exception) {
                Log.e("TrackCard", "Failed to load image: ${e.message}")
                null
            }
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
            .padding(vertical = 4.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect?.invoke() } // handles select
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Album image
        if (albumBitmap != null) {
            Image(
                bitmap = albumBitmap!!.asImageBitmap(),
                contentDescription = "Album image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            // Placeholder box while loading
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Gray)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        // Track info
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text( // Song name
                text = track.name,
                color = Color(0xFFC0EDFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bevan_regularFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text( // Album artist
                text = track.artists.joinToString { it.name },
                color = Color(0xFFC0EDFF),
                fontSize = 6.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text( // Album name
                text = track.album.name,
                color = Color.Gray,
                fontSize = 6.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val durationMinutes = track.durationMs / 1000 / 60
            val durationSeconds = (track.durationMs / 1000) % 60
            Text(
                text = "%d:%02d".format(durationMinutes, durationSeconds),
                color = Color.Gray,
                fontSize = 6.sp
            )
        }
    }
}

// Displays the selected song with playback functionality
@Composable
fun SelectedTrackCard(
    track: Track,
    playlistViewModel: PlaylistViewModel,
    isPlaying: Boolean = false,
    isRepeating: Boolean = false,
    isShuffling: Boolean = false,
    onPlayPauseClick: () -> Unit = {},
    onRestartClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onToggleRepeatClick: () -> Unit = {},
    onToggleShuffleClick: () -> Unit = {},
    moveFocusToButton: Boolean = false,
    modifier: Modifier
) {
    val moveFocus = remember { FocusRequester() }
    val selectedTrack by playlistViewModel.selectedTrack.collectAsState() // selected song
    val allPlaylists = PlaylistRepository.playlists
    val currentPlaylist by playlistViewModel.selectedPlaylist.collectAsState() // selected playlist

    // Available playlists that don’t already contain this track
    val availablePlaylists = allPlaylists.filter { playlist ->
        playlist.id != currentPlaylist?.id &&
                playlist.tracks.none { it.uri == track.uri }
    }

    // Dialogs
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showAddedMessage by remember { mutableStateOf(false) }

    // shift focus to the button when the track is selected
    LaunchedEffect(moveFocusToButton, track) {
        if (moveFocusToButton) moveFocus.requestFocus()
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xD0121212))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album image
            val albumBitmap = remember { mutableStateOf<Bitmap?>(null) }
            val imageUrl = track.album.images.firstOrNull()?.url
            // Load image
            LaunchedEffect(imageUrl) {
                if (!imageUrl.isNullOrEmpty()) {
                    try {
                        albumBitmap.value = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeStream(java.net.URL(imageUrl).openStream())
                        }
                    } catch (e: Exception) {
                        Log.e("SelectedTrackCard", "Failed to load image: ${e.message}")
                    }
                }
            }

            if (albumBitmap.value != null) {
                Image(
                    bitmap = albumBitmap.value!!.asImageBitmap(),
                    contentDescription = "Album image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            // Track info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Song Name
                Text(
                    text = track.name,
                    color = Color(0xFFC0EDFF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = bevan_regularFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Artist Name
                Text(
                    text = track.artists.joinToString { it.name },
                    color = Color(0xFFC0EDFF),
                    fontSize = 9.sp,
                    fontFamily = bevan_italicFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Album Name
                Text(
                    text = track.album.name,
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = bevan_italicFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val durationMinutes = track.durationMs / 1000 / 60
                val durationSeconds = (track.durationMs / 1000) % 60
                Text(
                    text = "%d:%02d".format(durationMinutes, durationSeconds),
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }

            // Playback controls + Add button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                PlaybackButton( // Shuffle playlist
                    onClick = onToggleShuffleClick,
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffling) Color(0x662DFF7A) else Color(0xFFC0EDFF),
                    size = 20.dp
                )
                PlaybackButton( // Repeat song
                    onClick = onToggleRepeatClick,
                    imageVector = if (isRepeating) Icons.Filled.RepeatOne else Icons.Outlined.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeating) Color(0x662DFF7A) else Color(0xFFC0EDFF),
                    size = 20.dp
                )
                PlaybackButton( // Previous Song
                    onClick = onPreviousClick,
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color(0xFFC0EDFF),
                    size = 24.dp
                )
                PlaybackButton( // Restart song
                    onClick = onRestartClick,
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Restart",
                    tint = Color(0xFFC0EDFF),
                    size = 28.dp
                )
                PlaybackButton( // Toggle Playback
                    onClick = onPlayPauseClick,
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFFC0EDFF),
                    size = 48.dp,
                    focusRequester = moveFocus
                )
                PlaybackButton( // Next Song
                    onClick = onNextClick,
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color(0xFFC0EDFF),
                    size = 28.dp
                )
                PlaybackButton( // Add to another playlist
                    onClick = { showAddToPlaylistDialog = true },
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to Playlist",
                    tint = Color(0xFFC0EDFF),
                    size = 24.dp
                )
                // Refresh the Tabs
                RefreshTabsButton(
                    selectedTrack = track,
                    playlistViewModel = playlistViewModel
                )
            }
        }

        // "Song added" confirmation bar
        if (showAddedMessage) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xDD1DA54F))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Song added to playlist", color = Color.White, fontFamily = bevan_regularFont)
            }
            // temp message, gone after 2 sec on success
            LaunchedEffect(showAddedMessage) {
                delay(2000)
                showAddedMessage = false
            }
        }
    }

    // Add to playlist dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            availablePlaylists = availablePlaylists,
            selectedTrack = track,
            playlistViewModel = playlistViewModel,
            onDismiss = { showAddToPlaylistDialog = false },
            onAdded = { showAddedMessage = true }
        )
    }
}

// This holds the WebView for the Tabs display
@Composable
fun TabsCard(
    title: String,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel,
    webViewRef: MutableState<WebView?>
) {
    // This collects the generated URL in the PlaylistViewModel for the Tab search
    val tabUrl by playlistViewModel.tabUrl.collectAsState()
    val tabRefresh by playlistViewModel.tabRefresh.collectAsState()
    // To stop JS operations from running multiple times
    val isPlaying by playlistViewModel.isPlaying.collectAsState()

    // Main Content Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xDD121212), shape = RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        if (title == "Tablatures") { // this if is redundant, but i'm too lazy to untangle it and its not breaking anything so it stays
            var tabProcessed by remember { mutableStateOf(false) }
            // The WebView, displayed inside an AndroidView to simulate a mobile interface in compose
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            // Fake Desktop
                            userAgentString =
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
                        }

                        webViewClient = object : WebViewClient() {

                            // Only fallback if main frame completely fails
                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    view?.loadUrl("https://www.songsterr.com/")
                                    playlistViewModel.clearTabUrl()
                                }
                            }

                            // Block known ad URLs to prevent flicker / reload
                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val blockedDomains = listOf(
                                    "doubleclick.net",
                                    "ads.google.com",
                                    "adservice.google.com",
                                    "googlesyndication.com",
                                    "adroll.com"
                                )
                                val url = request?.url.toString()
                                if (blockedDomains.any { url.contains(it) }) {
                                    return WebResourceResponse("text/plain", "utf-8", null)
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                            // Run the JS functions on the WebView when the url is loaded and the page is finished loading
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                if (tabProcessed) return
                                tabProcessed = true

                                view?.postDelayed({
                                    val js = """
                                        (function selectBassTrack() {
                                            try {
                                                // Desktop viewport
                                                var meta = document.querySelector('meta[name="viewport"]');
                                                if (!meta) { 
                                                    meta = document.createElement('meta'); 
                                                    meta.name='viewport'; 
                                                    document.head.appendChild(meta); 
                                                }
                                                var scale = document.documentElement.clientWidth / 1440;
                                                meta.setAttribute('content', 'width=1080, initial-scale=' + scale + ', user-scalable=no');
                                    
                                                // Remove ad boxes: Doesn't really work but doesn't hurt to try
                                                document.querySelectorAll("iframe, .ad, [id^='ad'], [class*='ad'], .ads, .ad-container").forEach(el => el.remove());
                                    
                                                // Click bass track persistently
                                                function clickBass() {
                                                    const bassLink = Array.from(document.querySelectorAll('a.Ccy4yp'))
                                                        .find(a => a.innerText.toLowerCase().includes('bass'));
                                                    if (bassLink) {
                                                        bassLink.scrollIntoView({behavior:'smooth', block:'center'});
                                                        bassLink.click();
                                        
                                                        // Set synth audio
                                                        const synthRadio = document.querySelector('#control-source input[value="synth"]');
                                                        if (synthRadio && !synthRadio.checked) synthRadio.click();
                                        
                                                        // Mute bass track
                                                        const muteBtn = document.querySelector('button.Bvv7a7[aria-label="Unmute sound"]');
                                                        if (muteBtn) muteBtn.click();
                                        
                                                        return true;
                                                    }
                                                    return false;
                                                }
                                                // select the bass track
                                                function openMixerAndClickBass() {
                                                    const mixerBtn = document.querySelector('#control-mixer');
                                                    if (mixerBtn) mixerBtn.click();
                                                    if (!clickBass()) setTimeout(openMixerAndClickBass, 250);
                                                }
                                                // select the first search result and click it
                                                const firstResult = document.querySelector('a.Ch01jq');
                                                if (firstResult) {
                                                    firstResult.click();
                                                    setTimeout(openMixerAndClickBass, 1000);
                                                } else {
                                                    openMixerAndClickBass();
                                                }
                                            } catch(e) {
                                                console.error(e);
                                                setTimeout(selectBassTrack, 500);
                                            }
                                        })();
                                    """.trimIndent()
                                    view.evaluateJavascript(js, null)
                                }, 500)
                            }
                        }
                        // Remove focus from WebView so you don't get stuck in it
                        isFocusable = false
                        isFocusableInTouchMode = false
                        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                        webViewRef.value = this
                    }
                },
                // Update the URL everytime it changes
                update = { webView ->
                    tabUrl?.let {
                        if (webView.url != it) {
                            tabProcessed = false
                            webView.loadUrl(it)
                        }
                    }
                }
            )
            // Sync the tabs with the Spotify Playback
            LaunchedEffect(webViewRef.value, isPlaying) {
                delay(200)
                webViewRef.value?.let { webView ->
                    toggleTabsPlayPause(webView, isPlaying)
                }
            }
            // reload the WebView with the selected URL
            LaunchedEffect(tabRefresh) {
                webViewRef.value?.let { webView ->
                    tabProcessed = false
                    webView.reload()
                }
            }
        } else {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

// This holds the cards that show playlists in the users library
@Composable
fun PlaylistsCard(
    playlists: List<Playlist>,
    playlistViewModel: PlaylistViewModel,
    modifier: Modifier = Modifier,
    onPlaylistSelect: ((Playlist) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .background(Color(0xD0121212))
            .border(1.dp, Color(0xDD121212), RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .padding(4.dp)
            .fillMaxSize()
    ) {
        if (playlists.isEmpty()) {
            // Show placeholder text if no playlists
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No playlists found",
                    color = Color(0xFFC0EDFF),
                    fontFamily = bevan_regularFont,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Show playlists in grid
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val rows = playlists.chunked(2)
                items(rows) { rowPlaylists ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (playlist in rowPlaylists) {
                            PlaylistCard(
                                playlist = playlist,
                                playlistViewModel = playlistViewModel,
                                onSelect = { onPlaylistSelect?.invoke(playlist) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPlaylists.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// The individual playlist items inside the display card ^
@Composable
fun PlaylistCard(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel,
    onSelect: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) } // focus state
    val selectedPlaylist by playlistViewModel.selectedPlaylist.collectAsState() // selected playlist
    val isSelectedPlaylist = selectedPlaylist?.name == playlist.name // for playlist border change

    val borderColor by animateColorAsState( // change border on selected and focused
        targetValue = when {
            isSelectedPlaylist -> Color(0xFFC0EDFF)
            isFocused -> Color(0xFF1DA54F)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300)
    )

    var playlistBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageUrl = playlist.images.firstOrNull()?.url
    val imageCache = remember { mutableMapOf<String, Bitmap>() }

    // Load image
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrEmpty()) {
            playlistBitmap = imageCache[imageUrl] ?: try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(java.net.URL(imageUrl).openStream())
                }
                imageCache[imageUrl] = bitmap
                bitmap
            } catch (e: Exception) {
                Log.e("PlaylistCard", "Failed to load image: ${e.message}")
                null
            }
        }
    }
    // The Playlist
    Box(
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect?.invoke() }
            .border(1.dp, borderColor, RoundedCornerShape(8.dp)) // border now reacts to selection
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image
            if (playlistBitmap != null) {
                Image(
                    bitmap = playlistBitmap!!.asImageBitmap(),
                    contentDescription = playlist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Playlist name
            Text(
                text = playlist.name,
                color = Color(0xFFC0EDFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bevan_regularFont,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Holds the songs in the selected playlist
@Composable
fun SongsManagementCard(
    playlist: Playlist,
    tracks: List<Track>,
    playlistViewModel: PlaylistViewModel,
    playbackViewModel: PlaybackViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onTrackSelect: ((Track) -> Unit)? = null
) {
    val spotifyPlayback by playbackViewModel.playbackState.collectAsState() // playback state
    val isSpotifyPlaying = spotifyPlayback?.isPaused?.not() ?: false
    val currentTrackUri = spotifyPlayback?.trackUri

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD0121212))
            .padding(4.dp)
    ) {
        if (tracks.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                items(tracks) { track ->
                    val isTrackPlaying = track.uri == currentTrackUri && isSpotifyPlaying

                    TrackManagementCard(
                        track = track,
                        playlistViewModel = playlistViewModel,
                        navController = navController,
                        onSelect = {
                            playlistViewModel.selectTrack(track)
                            onTrackSelect?.invoke(track)
                        },
                        playSong = {
                            playlistViewModel.selectTrack(track)
                            navController.navigate("tabs")
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks available",
                    color = Color(0xFFC0EDFF),
                    fontFamily = bevan_regularFont,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Displays individual tracks in the selected playlist with the management operations
@Composable
fun TrackManagementCard(
    track: Track,
    modifier: Modifier = Modifier,
    playlistViewModel: PlaylistViewModel,
    navController: NavHostController,
    onSelect: (() -> Unit)? = null,
    playSong: () -> Unit = {},
) {
    val selectedTrack by playlistViewModel.selectedTrack.collectAsState() // selected track
    val isSelectedTrack = selectedTrack?.uri == track.uri // check if track is selected for border change
    val allPlaylists = PlaylistRepository.playlists
    val currentPlaylist by playlistViewModel.selectedPlaylist.collectAsState() // selected playlist

    val availablePlaylists = allPlaylists.filter { playlist ->
        playlist.id != currentPlaylist?.id &&
                playlist.tracks.none { it.uri == selectedTrack?.uri }
    }

    // Add song dialogs
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showAddedMessage by remember { mutableStateOf(false) }

    var isFocused by remember { mutableStateOf(false) } // focus state
    val focusRequesterPlay = remember { FocusRequester() }
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelectedTrack -> Color(0xFFC0EDFF)
            isFocused -> Color(0xFF1DA54F)
            else -> Color.Transparent
        },

        animationSpec = tween(durationMillis = 300)
    )

    // Delete song dialogs
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeletedMessage by remember { mutableStateOf(false) }

    // Load album image as Bitmap asynchronously
    var albumBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageUrl = track.album.images.firstOrNull()?.url
    val imageCache = remember { mutableMapOf<String, Bitmap>() }

    // load image
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrEmpty()) {
            albumBitmap = imageCache[imageUrl] ?: try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(java.net.URL(imageUrl).openStream())
                }
                imageCache[imageUrl] = bitmap
                bitmap
            } catch (e: Exception) {
                Log.e("TrackManagementCard", "Failed to load image: ${e.message}")
                null
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 4.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable {
                playlistViewModel.selectTrack(track)
                onSelect?.invoke()
                focusRequesterPlay.requestFocus()
            }
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album image
        if (albumBitmap != null) {
            Image(
                bitmap = albumBitmap!!.asImageBitmap(),
                contentDescription = "Album image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Image",
                    color = Color.White,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Track info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text( // Song name
                text = track.name,
                color = Color(0xFFC0EDFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bevan_regularFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Artist name
                text = track.artists.joinToString { it.name },
                color = Color(0xFFC0EDFF),
                fontSize = 8.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Album name
                text = track.album.name,
                color = Color.Gray,
                fontSize = 8.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))


        // Action buttons row
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // play selected song with tabs
            PlaybackButton(
                onClick = {
                    // Navigate to tabs page with selected track and playlist
                    navController.navigate("tabs")
                },
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFFC0EDFF),
                size = 32.dp,
                focusRequester = focusRequesterPlay
            )
            // Add to another playlist
            PlaybackButton(
                onClick = {
                    if (isSelectedTrack) showAddToPlaylistDialog = true
                },
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFFC0EDFF),
                size = 32.dp
            )
            // Remove from current playlist
            PlaybackButton(
                onClick = {
                    if (isSelectedTrack) showDeleteDialog = true
                },
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFFC0EDFF),
                size = 32.dp
            )
        }
    }
    // Delete song dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Remove Song",
                    color = Color(0xFFC0EDFF) // title text color
                )
            },
            text = {
                Text(
                    "Are you sure you want to remove \"${track.name}\" from the playlist?",
                    color = Color(0xFFC0EDFF) // main text color
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        playlistViewModel.removeSelectedTrack()
                        showDeletedMessage = true
                    }
                ) {
                    Text(
                        "Yes",
                        color = Color(0xFF1DA54F) // confirm button color
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        "No",
                        color = Color(0xFF1DA54F) // dismiss button color
                    )
                }
            },
            containerColor = Color(0xFF121212) // background color of the dialog
        )
    }

    // Deleted confirmation message (temporary)
    if (showDeletedMessage) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xDD1DA54F))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Song removed from playlist",
                color = Color.White
            )
        }

        // hide after 2 sec
        LaunchedEffect(showDeletedMessage) {
            delay(2000)
            showDeletedMessage = false
        }
    }

    // Add to playlist dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            availablePlaylists = availablePlaylists,
            selectedTrack = selectedTrack,
            playlistViewModel = playlistViewModel,
            onDismiss = { showAddToPlaylistDialog = false },
            onAdded = { showAddedMessage = true }
        )
    }
    // Temp message, successfully added song
    if (showAddedMessage) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xDD1DA54F))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Song added to playlist", color = Color.White)
        }
        // hide after 2 sec
        LaunchedEffect(showAddedMessage) {
            delay(2000)
            showAddedMessage = false
        }
    }
}

// This is the card that shows the suggested song for the users selected genre
@Composable
fun SuggestedSongCard(
    playlistViewModel: PlaylistViewModel,
    navController: NavController,
    playbackViewModel: PlaybackViewModel
) {
    val selectedTrack by playlistViewModel.selectedTrack.collectAsState() // currently selected song
    var isFocused by remember { mutableStateOf(false) } // focus state
    val focusRequester = remember { FocusRequester() } // for focus change

    // Animate border when focused
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF1DA54F) else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )

    // Load album image
    var albumBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val imageUrl = selectedTrack?.album?.images?.firstOrNull()?.url
    val imageCache = remember { mutableMapOf<String, Bitmap>() }

    // Load the image for the song
    LaunchedEffect(imageUrl) {
        if (!imageUrl.isNullOrEmpty()) {
            albumBitmap = imageCache[imageUrl] ?: try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeStream(java.net.URL(imageUrl).openStream())
                }
                imageCache[imageUrl] = bitmap
                bitmap
            } catch (e: Exception) {
                Log.e("SuggestedSongCard", "Failed to load image: ${e.message}")
                null
            }
        }
    }

    // Auto-focus when the suggested song changes
    LaunchedEffect(selectedTrack) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 4.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album image or placeholder
        if (albumBitmap != null) {
            Image(
                bitmap = albumBitmap!!.asImageBitmap(),
                contentDescription = selectedTrack?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.Gray)
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Image",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontFamily = jomhuriaFont,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Track info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text( // Song Name
                text = selectedTrack?.name ?: "No song selected",
                color = Color(0xFFC0EDFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bevan_regularFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Song Artist
                text = selectedTrack?.artists?.joinToString { it.name } ?: "",
                color = Color(0xFFC0EDFF),
                fontSize = 10.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Album Name
                text = selectedTrack?.album?.name ?: "",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlaybackButton(
                onClick = {
                    val track = playlistViewModel.selectedTrack.value
                    val playlist = playlistViewModel.selectedPlaylist.value

                    if (track != null && playlist != null) {
                        // Sync selection & playback
                        playlistViewModel.selectTrack(track)
                        playlistViewModel.togglePlayPause(playbackViewModel)

                        // Navigate to TabsPage
                        navController.navigate("tabs")
                    } else {
                        Log.w(
                            "SuggestedSongCard",
                            "⚠️ No track or playlist selected before navigating to TabsPage"
                        )
                    }
                },
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFFC0EDFF),
                size = 40.dp
            )

            PlaybackButton(
                onClick = {
                    // suggest another song from the current playlist
                    playlistViewModel.selectedPlaylist.value?.let { playlist ->
                        playlistViewModel.suggestAnother(playlist)
                    }
                },
                imageVector = Icons.Default.Replay,
                contentDescription = "Reload",
                tint = Color(0xFFC0EDFF),
                size = 36.dp,
                focusRequester = focusRequester
            )
        }
    }
}

// This displays the info about the users available devices for playback
@Composable
fun DeviceCard(
    device: SpotifyDevice,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onSelect: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) } // focus state
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFFC0EDFF)
            isFocused -> Color(0xFF1DA54F)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300)
    )
    val deviceIcon = when (device.type.lowercase()) {
        "computer" -> Icons.Default.Computer
        "smartphone" -> Icons.Default.Smartphone
        "tablet" -> Icons.Default.Tablet
        "speaker" -> Icons.Default.Speaker
        "tv" -> Icons.Default.Cast
        else -> Icons.Default.Devices
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(75.dp)
            .padding(vertical = 4.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelect() }
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device icon / placeholder
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = deviceIcon, // replace with your icon
                contentDescription = "Device icon",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Device info
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text( // Name
                text = device.name,
                color = Color(0xFFC0EDFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = bevan_regularFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Type
                text = device.type,
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = bevan_italicFont,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text( // Active Status
                text = if (device.is_active) "Active" else "Inactive",
                color = if (device.is_active) Color(0xFF1DB954) else Color.Gray,
                fontSize = 10.sp,
                fontFamily = bevan_italicFont
            )
        }
    }
}