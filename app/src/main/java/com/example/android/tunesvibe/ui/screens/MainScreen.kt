package com.example.android.tunesvibe.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.android.tunesvibe.PlaybackScreenRoute
import com.example.android.tunesvibe.R
import com.example.android.tunesvibe.service.AudioService
import com.example.android.tunesvibe.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@UnstableApi
@ExperimentalMaterial3Api
@Composable
fun MainScreen(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val audiosList by mainViewModel.audiosList.collectAsState()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    var isVisible by remember {
        mutableStateOf(true)
    }
    var index by remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(Unit) {
        val intent = Intent(context, AudioService::class.java)
        context.startService(intent)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tunes Vibe",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "search",
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                        contentDescription = "search",
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(vertical = 16.dp)
        ) {
            index =
                context.getSharedPreferences("audio index", Context.MODE_PRIVATE).getInt("index", 0)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(
                            PlaybackScreenRoute(
                                index = index,
                                title = audiosList[index].title,
                                artist = audiosList[index].artist,
                                data = audiosList[index].data,
                                albumArtString = audiosList[index].albumArtUri.toString()
                            )
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "back",
                    modifier = Modifier
                        .weight(0.1f)
                        .clickable {
                            index--
                            mainViewModel.saveLastAudio(context, index)
                        }
                )
                val albumArtUri = audiosList[index].albumArtUri
                Row {
                    if (albumArtUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = albumArtUri),
                            contentDescription = "artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(DpSize(100.dp, 100.dp))
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.music_background),
                            contentDescription = "artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .size(DpSize(100.dp, 100.dp))
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                    ) {
                        Text(
                            text = truncatedText(audiosList[index].title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.opensans))
                        )
                        Text(
                            text = audiosList[index].artist,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.opensans))
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                    contentDescription = "next",
                    modifier = Modifier
                        .weight(0.1f)
                        .clickable {
                            index++
                            mainViewModel.saveLastAudio(context, index)
                        }
                )
            }
            LazyColumn(
                state = lazyListState
            ) {
                itemsIndexed(
                    items = audiosList,
                    key = { _, audio ->
                        audio.id
                        audio.data
                        audio.title
                    }
                ) { index, audio ->

                    AudioItem(
                        index = index + 1,
                        title = audio.title,
                        artist = audio.artist,
                        albumUri = audio.albumArtUri
                    ) {
                        navController.navigate(
                            PlaybackScreenRoute(
                                index = index,
                                title = audio.title,
                                artist = audio.artist,
                                data = audio.data,
                                albumArtString = audio.albumArtUri.toString()
                            )
                        )
                        mainViewModel.saveLastAudio(context, index)
                    }
                }
            }
        }
    }
}

@Composable
fun AudioItem(
    index: Int,
    title: String,
    artist: String,
    albumUri: Uri?,
    onItemSelected: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(end = 16.dp)
            .clickable {
                onItemSelected()
            }
    ) {
        Text(
            text = index.toString(),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(0.13f)
        )
        if (albumUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = albumUri),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(70.dp)
                    .height(70.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.music_background),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(70.dp)
                    .height(70.dp)
                    .clip(CircleShape)
            )
        }
        Column(
            modifier = Modifier
                .weight(0.8f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = artist,
                fontSize = 16.sp
            )
        }
    }
}

fun truncatedText(text: String): String {
    val words = text.split(" ", "_")
    return if (words.size > 6) {
        "${words[0]} ${words[1]} ${words[2]} ${words[3]} ${words[4]} ${words[5]} ..."
    } else {
        text
    }
}