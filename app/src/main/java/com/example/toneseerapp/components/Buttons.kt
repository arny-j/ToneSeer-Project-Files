package com.example.toneseerapp.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistViewModel
import com.example.toneseerapp.data.Track
import com.example.toneseerapp.jomhuriaFont

// These buttons are used to control the playback options, and sometimes other functions, they are circular with no borders
@Composable
fun PlaybackButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String,
    tint: Color,
    size: Dp = 24.dp,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) } // Focus state
    val borderColor by animateColorAsState(  // Border color animation
        targetValue = if (isFocused) Color(0x662DFF7A) else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )
    // The playback button
    IconButton(
        onClick = onClick, // function to run
        modifier = Modifier
            .size(size)
            .then(
                if (focusRequester != null) Modifier.focusRequester(focusRequester) // for items that require the focusRequester
                else Modifier
            )
            .onFocusChanged { isFocused = it.isFocused } // set focus state
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // This icon that the button will show
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size * 0.8f) // slightly smaller to fit the border
            )
        }
    }
}

// Button specifically for refreshing the tabs when they fail or load incorrectly
@SuppressLint("RememberInComposition")
@Composable
fun RefreshTabsButton(
    selectedTrack: Track?,
    playlistViewModel: PlaylistViewModel,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) } // Focus state
    val borderColor by animateColorAsState( // Border color animation
        targetValue = if (isFocused) Color(0xFF1DA54F) else Color.Transparent,
        animationSpec = tween(durationMillis = 300)
    )

    Box(
        modifier = Modifier
            // Focusable with requester optional
            .focusRequester(focusRequester ?: FocusRequester())
            .onFocusChanged { isFocused = it.isFocused }
            .clickable {
                playlistViewModel.refreshTabs() // trigger the reload
            }
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) { 
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The reload icon
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Tabs",
                tint = if (selectedTrack == null || selectedTrack.id == "placeholder")
                    Color.Gray
                else
                    Color(0xFFC0EDFF),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            // Text
            Text(
                text = "Tabs",
                color = if (selectedTrack == null || selectedTrack.id == "placeholder")
                    Color.Gray
                else
                    Color(0xFFC0EDFF),
                fontSize = 12.sp
            )
        }
    }
}

// This is the common button item, where any "normal" buttons need to be used
@Composable
fun ActionButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) } // Focus state
    val borderColor by animateColorAsState( // Border Color animation for both focus and unfocus
        targetValue = if(isFocused) Color(0xFF1DA54F) else Color(0xFFC0EDFF),
        animationSpec = tween(durationMillis = 300),
        label = "BorderColorAnimation"
    )
    val scale by animateFloatAsState( // Size animation for focus
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "ScaleAnimation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .width(170.dp)
            .height(40.dp)
            .background(Color(0xD0121212), shape = RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = jomhuriaFont,
            fontSize = 25.sp,
            color = Color(0xFFC0EDFF),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// This is the ButtonRow for the TabsPage
@Composable
fun TabPageButtonRow(
    modifier: Modifier = Modifier,
    selectedTrack: Track?,
    playlistViewModel: PlaylistViewModel
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        RefreshTabsButton(
            selectedTrack = selectedTrack,
            playlistViewModel = playlistViewModel
        )
    }
}

// The Button row for the Playlists page
@Composable
fun PlaylistPageButtonRow(
    navController: NavController,
    playlistViewModel: PlaylistViewModel
) {
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionButton("Create Playlist", Modifier.weight(1f)) {
            showCreateDialog = true // Dialog for creating a new playlist
        }

        ActionButton("Delete Playlist", Modifier.weight(1f)) {
            showDeletePlaylistDialog = true // Dialog for deleting a playlist
        }
    }

    // run the dialogs
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description ->
                playlistViewModel.createPlaylist(name, description)
                showCreateDialog = false
            }
        )
    }
    if (showDeletePlaylistDialog) {
        DeletePlaylistDialog(
            playlistViewModel = playlistViewModel,
            onDismiss = { showDeletePlaylistDialog = false }
        )
    }
}

// The Two columns of buttons for the Song Suggestion Page
@Composable
fun SongSuggestionButtons(playlistViewModel: PlaylistViewModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ToneSeer genre playlists
            ActionButton("Rock") {
                playlistViewModel.suggestRandomSong(genre = "Rock")
            }
            ActionButton("Pop") {
                playlistViewModel.suggestRandomSong(genre = "Pop")
            }
            ActionButton("Grunge") {
                playlistViewModel.suggestRandomSong(genre = "Grunge")
            }
            ActionButton("Jazz") {
                playlistViewModel.suggestRandomSong(genre = "Jazz")
            }
            ActionButton("Country") {
                playlistViewModel.suggestRandomSong(genre = "Country")
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ActionButton("Metal") {
                playlistViewModel.suggestRandomSong(genre = "Metal")
            }
            ActionButton("Funk") {
                playlistViewModel.suggestRandomSong(genre = "Funk")
            }
            ActionButton("Reggae") {
                playlistViewModel.suggestRandomSong(genre = "Reggae")
            }
            ActionButton("Indie") {
                playlistViewModel.suggestRandomSong(genre = "Indie")
            }
            ActionButton("Punk") {
                playlistViewModel.suggestRandomSong(genre = "Punk")
            }
        }
    }
}