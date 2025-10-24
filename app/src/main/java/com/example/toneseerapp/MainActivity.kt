package com.example.toneseerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.toneseerapp.pages.HomeScreen
import com.example.toneseerapp.pages.PlaylistPage
import com.example.toneseerapp.pages.SongSuggestionPage
import com.example.toneseerapp.pages.SpotifyLoginPage
import com.example.toneseerapp.pages.TabsPage
import com.example.toneseerapp.ui.theme.ToneSeerAppTheme
import com.example.toneseerapp.data.PlaybackViewModel
import com.example.toneseerapp.data.PlaylistViewModel

// Fonts
val jomhuriaFont = FontFamily(
    Font(R.font.jomhuria_regular)
)
val bevan_regularFont = FontFamily(
    Font(R.font.bevan_regular)
)
val  bevan_italicFont = FontFamily(
    Font(R.font.bevan_italic)
)
//--------------------------------------------------------------------------------------------------
// Main activity - The app itself
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow the pages that need access to the ViewModels
        val playbackViewModel: PlaybackViewModel by viewModels()
        val playlistViewModel: PlaylistViewModel by viewModels()

        setContent {
            ToneSeerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    // To control navigation for each page
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // Navigate Home
                        composable("home") { HomeScreen(navController=navController, playlistViewModel, playbackViewModel) }
                        // Navigate to Tabs Display
                        composable("tabs") { TabsPage(navController=navController, playlistViewModel, playbackViewModel) }
                        // Navigate to the Playlists page
                        composable("playlists") { PlaylistPage(navController=navController, playlistViewModel, playbackViewModel)}
                        // Navigate to the song Suggestion page
                        composable("song_suggestion") { SongSuggestionPage(navController=navController, playlistViewModel, playbackViewModel)}
                        // Navigate to Spotify Login QR
                        composable("spotify_login") { SpotifyLoginPage(navController=navController, playbackViewModel, playlistViewModel)}
                    }
                }
            }
        }
    }
}
