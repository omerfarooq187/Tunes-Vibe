package com.example.android.tunesvibe.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.android.tunesvibe.PlaybackScreenRoute
import com.example.android.tunesvibe.R
import com.example.android.tunesvibe.ServiceConnectionHolder
import com.example.android.tunesvibe.service.AudioService
import com.example.android.tunesvibe.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(
    args: PlaybackScreenRoute,
    serviceConnectionHolder: ServiceConnectionHolder,
    navController: NavController
) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val audiosList by mainViewModel.audiosList.collectAsState()
    val serviceConnection = remember {
        serviceConnectionHolder
    }
    var title by remember {
        mutableStateOf(args.title)
    }
    var artist by remember {
        mutableStateOf(args.artist)
    }
    val currentSongIndex by remember {
        mutableIntStateOf(args.index)
    }
    val albumArtString by remember {
        mutableStateOf(args.albumArtString)
    }
    var albumArtUri by remember {
        mutableStateOf<Uri?>(null)
    }
    if (albumArtString != null) {
        albumArtUri = Uri.parse(albumArtString)
    }
    var duration by remember {
        mutableLongStateOf(0)
    }
    var currentPosition by remember {
        mutableLongStateOf(0)
    }
    var isPlaying by remember {
        mutableStateOf(false)
    }
    var isClickable by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val intent = Intent(context, AudioService::class.java)
    LaunchedEffect(Unit) {
        intent.putExtra("index", currentSongIndex)
        intent.action = AudioService.ACTIONS.PLAY.toString()
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    LaunchedEffect(isClickable) {
        if (!isClickable) {
            delay(500)
            isClickable = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(serviceConnection.audioService) {
        serviceConnection.onServiceConnected = {
            serviceConnection.audioService?.let { audioService ->
                scope.launch {
                    audioService.duration.collect { newDuration ->
                        duration = newDuration
                    }
                }
                scope.launch {
                    audioService.currentPosition.collect { newPosition ->
                        currentPosition = newPosition
                    }
                }
                scope.launch {
                    audioService.isPlaying.collect {
                        isPlaying = it
                    }
                }
                scope.launch {
                    audioService.currentIndex.collect { newCurrentIndex ->
                        title = audiosList[newCurrentIndex].title
                        artist = audiosList[newCurrentIndex].artist
                        albumArtUri = audiosList[newCurrentIndex].albumArtUri
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Now Playing",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.quicksand)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(end = 40.dp)
                            .fillMaxWidth()
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "",
                        modifier = Modifier
                            .height(30.dp)
                            .width(40.dp)
                            .clickable {
                                navController.popBackStack()
                            }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (albumArtUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = albumArtUri),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .height(160.dp)
                        .width(160.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.music_background),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .height(160.dp)
                        .width(160.dp)
                        .clip(CircleShape)
                )
            }

            Text(
                text = title,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.opensans)),
                fontSize = 22.sp,
                modifier = Modifier
                    .basicMarquee()
            )
            Text(
                text = artist,
                color = Color.Black,
                fontFamily = FontFamily(Font(R.font.quicksand)),
                fontSize = 20.sp
            )

            BouncingBars(isPlaying = isPlaying, modifier = Modifier.weight(0.2f))
            Column(
                modifier = Modifier
                    .weight(0.4f)
            ) {
                Slider(
                    value = currentPosition.toFloat(),
                    valueRange = 0f..duration.toFloat(),
                    onValueChange = { floatValue ->
                        serviceConnection.audioService?.seekTo(floatValue)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = currentPosition.toMinutesAndSeconds()
                    )
                    Text(
                        text = duration.toMinutesAndSeconds()
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(40.dp)
                            .weight(1.7f)
                            .clickable {
                                if (isClickable) {
                                    intent.action = AudioService.ACTIONS.SKIP_PREVIOUS.toString()
                                    context.startService(intent)
                                    isClickable = false
                                }
                            }
                    )
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(40.dp)
                            .weight(2f)
                            .clickable {
                                if (isPlaying) {
                                    intent.action = AudioService.ACTIONS.PAUSE.toString()
                                    context.startService(intent)
                                } else {
                                    intent.action = AudioService.ACTIONS.RESUME.toString()
                                    context.startService(intent)
                                }
                            }
                    )
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(40.dp)
                            .weight(1.7f)
                            .clickable {
                                if (isClickable) {
                                    intent.action = AudioService.ACTIONS.SKIP_NEXT.toString()
                                    context.startService(intent)
                                    isClickable = false
                                }
                            }
                    )
                }
            }
        }
    }

}

@SuppressLint("DefaultLocale")
private fun Long.toMinutesAndSeconds(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun BouncingBars(isPlaying: Boolean, modifier: Modifier) {
    val random = remember { Random.Default }
    val bars = 30 // Number of bars

    // Generate random heights for each bar
    val initialHeights = remember { List(bars) { random.nextFloat() * 30 + 15 } }
    val targetHeights = remember { List(bars) { random.nextFloat() * 60 + 30 } }

    // Create individual infinite transitions for each bar
    val infiniteTransitions = List(bars) { rememberInfiniteTransition(label = "audio bars") }
    val barHeights = infiniteTransitions.mapIndexed { index, transition ->
        transition.animateFloat(
            initialValue = initialHeights[index],
            targetValue = if (isPlaying) targetHeights[index] else initialHeights[index],
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "audio bars"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .padding(16.dp)
    ) {
        barHeights.forEach { barHeight ->
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = barHeight.value.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
