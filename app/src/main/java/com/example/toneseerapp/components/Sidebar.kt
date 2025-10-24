package com.example.toneseerapp.components

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.toneseerapp.R
import com.example.toneseerapp.bevan_regularFont

// Extendable Sidebar to help with navigation
@Composable
fun Sidebar(
    onNavigateHome: () -> Unit,
    onNavigatePlaylists: () -> Unit,
    onNavigateSongSug: () -> Unit,
    onNavigateSpotifyLogin: () -> Unit,
    //onNavigateYouTube: () -> Unit,
    //onNavigateSpotify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var anyChildFocused by remember { mutableStateOf(false) }
    var focusedCount by remember { mutableStateOf(0) }

    //    This stops the sidebar from collapsing when swapping between items
    fun updateFocus(isFocused: Boolean) {
        focusedCount = (focusedCount + if (isFocused) 1 else -1).coerceAtLeast(0)
        anyChildFocused = focusedCount > 0
    }
    val sidebarWidth by animateDpAsState(
        targetValue = if (anyChildFocused) 130.dp else 60.dp,
        label = "sidebarWidthAnimation"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            // This changes the size of the box when toggled
            .width(sidebarWidth)
            .background(
                // This creates a gradiant from left to right in the sidebar
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF101522),  // Start color (left)
                        Color(0xF5101522),
                        Color(0xE1101522),
                        Color(0xC3101522),
                        Color(0xBC101522),
                        Color(0xAB101522),
                        Color(0x9C101522),
                        Color.Transparent   // End color (right)
                    )
                )
            )
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Sidebar Logo",
                modifier = Modifier
                    .size(75.dp)
                    .padding(bottom = 10.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // These are the navigation items
            SidebarNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color(0xFFC0EDFF),
                        modifier = Modifier.size(24.dp)
                    )
                },
                logo = null,
                label = "Home",
                expanded = anyChildFocused,
                onClick = onNavigateHome,
                onFocusChanged = { updateFocus(it) }
            )
            SidebarNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = "Playlists",
                        tint = Color(0xFFC0EDFF),
                        modifier = Modifier.size(24.dp)
                    )
                },
                logo = null,
                label = "Playlists",
                expanded = anyChildFocused,
                onClick = onNavigatePlaylists,
                onFocusChanged = { updateFocus(it) }
            )

            SidebarNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Song Suggestion",
                        tint = Color(0xFFC0EDFF),
                        modifier = Modifier.size(24.dp)
                    )
                },
                logo = null,
                label = "Song Suggestion",
                expanded = anyChildFocused,
                onClick = onNavigateSongSug,
                onFocusChanged = { updateFocus(it) }
            )
            SidebarNavItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.spotify_logo_green_rgb),
                        contentDescription = "Spotify Icon",
                        modifier = Modifier.size(24.dp)
                    )
                },
                logo = {
                    Image(
                        painter = painterResource(id = R.drawable.spotify_full_logo_green_rgb),
                        contentDescription = "Spotify Logo",
                        modifier = Modifier.height(20.dp)
                    )
                },
                label = "Spotify",
                expanded = anyChildFocused,
                // TODO: Fix onClick
                onClick = {
                    launchTvApp(context, "com.spotify.tv.android")
                },
                onFocusChanged = { updateFocus(it) }
            )
            SidebarNavItem(
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.yt_icon_red_digital),
                        contentDescription = "YouTube Icon",
                        modifier = Modifier.size(28.dp)
                    )
                },
                logo = {
                    Image(
                        painter = painterResource(id = R.drawable.yt_logo_fullcolor_white_digital),
                        contentDescription = "YouTube Logo",
                        modifier = Modifier.height(24.dp)
                    )
                },
                label = "YouTube",
                expanded = anyChildFocused,
                // TODO: Fix onClick
                onClick = { launchYouTubeTv(context) },
                onFocusChanged = { updateFocus(it) }
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // space before bottom item
            SidebarNavItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Spotify Login",
                        tint = Color(0xFFC0EDFF),
                        modifier = Modifier.size(24.dp)
                    )
                },
                logo = null,
                label = "Spotify Login",
                expanded = anyChildFocused,
                onClick = onNavigateSpotifyLogin,
                onFocusChanged = { updateFocus(it) }
            )
        }
    }
}

// This is the component for each of the items in the Sidebar
@Composable
fun SidebarNavItem(
    icon: @Composable () -> Unit,   // what to show when collapsed
    logo: (@Composable () -> Unit)? = null, // what to show when expanded
    label: String,
    expanded: Boolean,
    onClick: (() -> Unit)? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var isFocused by remember { mutableStateOf(false) } // focus state

    val backgroundColor by animateColorAsState(
        if (isFocused) Color(0xD0121212) else Color.Transparent,
        label = "focusColorAnimation"
    )
    val borderColor by animateColorAsState(
        if (isFocused) Color(0xFF1DA54F) else Color.Transparent, // bright green when focused
        label = "focusBorderAnimation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 6.dp, vertical = 11.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged?.invoke(focusState.isFocused)
            }
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() } else Modifier
            )
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Collapsed = icon
        if (!expanded) {
            icon()
        } else {
            // Expanded = logo if provided, otherwise fallback to icon + text
            if (logo != null) {
                logo()
            } else {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontFamily = bevan_regularFont,
                    fontSize = 8.sp,
                    color = Color(0xFFC0EDFF),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 10.sp
                    )
                )
            }
        }
    }
}

// TODO: Get these actually opening the respective apps
// TV app launcher helper
fun launchTvApp(context: Context, packageName: String) {
    try {
        val intent = when (packageName) {
            "com.spotify.tv.android" -> {
                Intent().apply {
                    component = ComponentName(
                        "com.spotify.tv.android",
                        "com.spotify.tv.android.SpotifyTvActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "com.google.android.youtube.tv" -> {
                Intent().apply {
                    component = ComponentName(
                        "com.google.android.youtube.tv",
                        "com.google.android.youtube.tv.MainActivity"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            else -> null
        }

        if (intent != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "App not supported", Toast.LENGTH_SHORT).show()
        }
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
    }
}

// Suposed to open the YouTube app but hasn't worked yet
fun launchYouTubeTv(context: Context) {
    val pm = context.packageManager
    val possiblePackages = listOf(
        "com.google.android.youtube.tv",  // TV app
        "com.google.android.youtube"      // fallback mobile app
    )

    var launched = false
    for (pkg in possiblePackages) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) {
            context.startActivity(intent)
            launched = true
            break
        }
    }

    if (!launched) {
        Toast.makeText(context, "YouTube app not installed", Toast.LENGTH_SHORT).show()
    }
}
