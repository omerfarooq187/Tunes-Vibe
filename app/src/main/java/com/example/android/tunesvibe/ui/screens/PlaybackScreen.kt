package com.example.android.tunesvibe.ui.screens

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import coil.compose.rememberAsyncImagePainter
import com.example.android.tunesvibe.PlaybackScreenRoute
import com.example.android.tunesvibe.R
import com.example.android.tunesvibe.service.AudioService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackScreen(args: PlaybackScreenRoute) {
    val serviceConnection = remember {
        ServiceConnectionHolder()
    }
    val title by remember {
        mutableStateOf(args.title)
    }
    val artist by remember {
        mutableStateOf(args.artist)
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
        mutableIntStateOf(0)
    }
    var currentPosition by remember {
        mutableIntStateOf(0)
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val intent = Intent(context, AudioService::class.java)
        intent.putExtra("songUri", args.data)
        intent.action = AudioService.ACTIONS.PLAY.toString()
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(serviceConnection) {
        serviceConnection.onServiceConnected = {
            val audioService = serviceConnection.audioService
            scope.launch {
                audioService?.let {
                    it.duration.collect { newDuration ->
                        duration = newDuration
                    }
                }
            }
            scope.launch {
                audioService?.let {
                    it.currentPosition.collect { newPosition ->
                        currentPosition = newPosition
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
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "",
//                    modifier = Modifier
//                        .height(30.dp)
//                        .width(40.dp)
//                )
//                Text(
//                    text = "Now Playing",
//                    color = Color.Black,
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.Bold,
//                    fontFamily = FontFamily(Font(R.font.quicksand)),
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .weight(1f)
//                )
//            }
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
                    .padding(horizontal = 12.dp)
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
                        .weight(1.5f)
                )
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(40.dp)
                        .weight(2f)
                )
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(40.dp)
                        .weight(1.5f)
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun Int.toMinutesAndSeconds(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%02d:%02d", minutes, seconds)
}

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