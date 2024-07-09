package com.example.android.tunesvibe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.android.tunesvibe.ui.screens.MainScreen
import com.example.android.tunesvibe.ui.screens.PlaybackScreen
import com.example.android.tunesvibe.ui.theme.TunesVibeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable


@Serializable
object MainScreenRoute
@Serializable
data class PlaybackScreenRoute(
    val index: Int,
    val title: String,
    val artist: String,
    val data:String,
    val albumArtString: String? = null
)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                1
            )
        }
        else if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        setContent {
            TunesVibeTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> {
            MainScreen(navController)
        }
        composable<PlaybackScreenRoute> {
            val args: PlaybackScreenRoute = it.toRoute()
            PlaybackScreen(args)
        }
    }
}