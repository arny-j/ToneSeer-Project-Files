package com.example.toneseerapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toneseerapp.data.Playlist
import com.example.toneseerapp.data.PlaylistRepository
import com.example.toneseerapp.data.PlaylistViewModel
import com.example.toneseerapp.data.Track
import kotlinx.coroutines.delay

// The labels for the playlists displayed in the dialogs
@Composable
private fun PlaylistItem(
    playlist: Playlist,
    selectedTrack: Track?,
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit,
    onAdded: () -> Unit,
    focusRequester: FocusRequester? = null
) {
    var isFocused by remember { mutableStateOf(false) } // focus state

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .background(
                if (isFocused) Color(0xDF1DA54F) else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .clickable {
                selectedTrack?.let { track ->
                    playlistViewModel.addTrackToPlaylist(track, playlist)
                    onDismiss()
                    onAdded()
                }
            }
            .padding(vertical = 8.dp, horizontal = 8.dp)
    ) {
        Text(text = playlist.name, color = Color(0xFFC0EDFF))
    }

    Divider(color = Color(0x33FFFFFF))
}

// Dialog for adding a song to a playlist
@Composable
fun AddToPlaylistDialog(
    availablePlaylists: List<Playlist>,
    selectedTrack: Track?,
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit,
    onAdded: () -> Unit
) {
    // Filters for playlists
    val progressPlaylists = availablePlaylists.filter { it.name.startsWith("TS -") }
    val userPlaylists = availablePlaylists.filterNot { it.name.startsWith("TS -") }

    // Create a FocusRequester for the first progress playlist item
    val firstProgressFocusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Playlist", color = Color(0xFFC0EDFF)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Progress Playlists Section
                if (progressPlaylists.isNotEmpty()) {
                    item(key = "progressHeader") {
                        Text(
                            text = "Progress Playlists",
                            color = Color(0xFF1DA54F),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                        Divider(color = Color(0x33FFFFFF))
                    }

                    itemsIndexed(
                        items = progressPlaylists,
                        key = { _, playlist -> "progress_${playlist.id}" }
                    ) { index, playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            selectedTrack = selectedTrack,
                            playlistViewModel = playlistViewModel,
                            onDismiss = onDismiss,
                            onAdded = onAdded,
                            focusRequester = if (index == 0) firstProgressFocusRequester else null
                        )
                    }

                    // Splits the user playlists and progress playlists
                    item(key = "progressDivider") {
                        Divider(color = Color(0x33FFFFFF))
                    }
                }

                // User Playlists Section
                if (userPlaylists.isNotEmpty()) {
                    item(key = "userHeader") {
                        Text(
                            text = "User Playlists",
                            color = Color(0xFF1DA54F),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                        )
                        Divider(color = Color(0x33FFFFFF))
                    }

                    items(
                        items = userPlaylists,
                        key = { playlist -> "user_${playlist.id}" }
                    ) { playlist ->
                        PlaylistItem(
                            playlist = playlist,
                            selectedTrack = selectedTrack,
                            playlistViewModel = playlistViewModel,
                            onDismiss = onDismiss,
                            onAdded = onAdded
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF1DA54F))
            }
        },
        containerColor = Color(0xFF121212)
    )

    // Request focus once the dialog appears
    LaunchedEffect(Unit) {
        if (progressPlaylists.isNotEmpty()) {
            delay(150) // brief delay for layout
            firstProgressFocusRequester.requestFocus()
        }
    }
}

// Dialog for deleting a playlist from a users library
@Composable
fun DeletePlaylistDialog(
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit
) {
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Filter out any "TS -" playlists so they don't appear
    val playlists = PlaylistRepository.playlists.filterNot { it.name.startsWith("TS -") }

    // Focus requester for the first item
    val focusRequester = remember { FocusRequester() }
    // Request focus automatically when dialog shows and playlists exist
    LaunchedEffect(Unit) {
        if (playlists.isNotEmpty()) {
            focusRequester.requestFocus()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Playlist", color = Color(0xFFC0EDFF)) },
        text = {
            if (playlists.isEmpty()) {
                Text(
                    "No playlists available to delete.",
                    color = Color(0xFFC0EDFF)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
                        var isFocused by remember { mutableStateOf(false) } // focus state

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .background(
                                    if (isFocused) Color(0xDF1DA54F) else Color.Transparent,
                                    RoundedCornerShape(4.dp)
                                )
                                .onFocusChanged { isFocused = it.isFocused }
                                // Only the first item gets the FocusRequester attached
                                .then(
                                    if (index == 0) Modifier.focusRequester(focusRequester)
                                    else Modifier
                                )
                                .clickable {
                                    selectedPlaylist = playlist
                                    showConfirmDialog = true
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = playlist.name,
                                color = Color(0xFFC0EDFF)
                            )
                        }

                        Divider(color = Color(0x33FFFFFF))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF1DA54F))
            }
        },
        containerColor = Color(0xFF121212)
    )

    // Show confirmation dialog when user selects a playlist
    if (showConfirmDialog && selectedPlaylist != null) {
        ConfirmDeletePlaylistDialog(
            playlist = selectedPlaylist!!,
            playlistViewModel = playlistViewModel,
            onConfirm = {
                playlistViewModel.deletePlaylist(selectedPlaylist!!)
                showConfirmDialog = false
                onDismiss()
            },
            onCancel = { showConfirmDialog = false }
        )
    }
}

// To confirm the user wants to delete a playlist
@Composable
fun ConfirmDeletePlaylistDialog(
    playlist: Playlist,
    playlistViewModel: PlaylistViewModel,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Confirm Deletion", color = Color(0xFFC0EDFF)) },
        text = {
            Text(
                "Are you sure you want to delete \"${playlist.name}\"?\nThis will remove it from Spotify and your app.",
                color = Color(0xFFC0EDFF)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes", color = Color(0xFF1DA54F))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("No", color = Color(0xFF1DA54F))
            }
        },
        containerColor = Color(0xFF121212)
    )
}

// Dialog to create a Playlist
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var nameFocused by remember { mutableStateOf(false) }
    var descFocused by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Playlist", color = Color(0xFFC0EDFF))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Playlist Name Field (required)
                TextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Playlist Name", color = Color(0xFFC0EDFF)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedIndicatorColor = Color(0xFF1DA54F),
                        unfocusedIndicatorColor = Color(0xFFC0EDFF),
                        focusedTextColor = Color(0xFFC0EDFF),
                        unfocusedTextColor = Color(0xFFC0EDFF),
                        cursorColor = Color(0xFF1DA54F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { nameFocused = it.isFocused }
                )

                // Playlist Description Field (optional)
                TextField(
                    value = playlistDescription,
                    onValueChange = { playlistDescription = it },
                    label = { Text("Description (optional)", color = Color(0xFFC0EDFF)) },
                    singleLine = false,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedIndicatorColor = Color(0xFF1DA54F),
                        unfocusedIndicatorColor = Color(0xFFC0EDFF),
                        focusedTextColor = Color(0xFFC0EDFF),
                        unfocusedTextColor = Color(0xFFC0EDFF),
                        cursorColor = Color(0xFF1DA54F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp)
                        .onFocusChanged { descFocused = it.isFocused }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onCreate(playlistName.trim(), playlistDescription.takeIf { it.isNotBlank() })
                    }
                }
            ) {
                Text("Create", color = Color(0xFF1DA54F))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFC0EDFF))
            }
        },
        containerColor = Color(0xFF121212)
    )
}

