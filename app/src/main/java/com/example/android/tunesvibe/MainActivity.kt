package com.example.android.tunesvibe

import android.Manifest
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.android.tunesvibe.service.AudioService
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
    val data: String,
    val albumArtString: String? = null
)
@ExperimentalMaterial3Api
@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TunesVibeTheme {
                var permissionsGranted by remember {
                    mutableStateOf(false)
                }
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions: Map<String, Boolean> ->
                    permissionsGranted = permissions.all {
                        it.value
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )
                    } else {
                        permissionsLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        )
                    }
                }

                if (permissionsGranted) {
                    App()
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@UnstableApi
@Composable
fun App() {
    val serviceConnection = ServiceConnectionHolder()
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = MainScreenRoute) {
        composable<MainScreenRoute> {
            MainScreen(navController)
        }
        composable<PlaybackScreenRoute> {
            val args: PlaybackScreenRoute = it.toRoute()
            PlaybackScreen(args, serviceConnection, navController)
        }
    }
}

@UnstableApi
class ServiceConnectionHolder : ServiceConnection {

    var audioService: AudioService? = null
    var onServiceConnected: (() -> Unit)? = null
    override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
        val binder = service as AudioService.LocalBinder
        audioService = binder.getService()
        onServiceConnected?.invoke()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        audioService = null
    }
}
