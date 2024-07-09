package com.example.android.tunesvibe.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.android.tunesvibe.PlaybackScreenRoute
import com.example.android.tunesvibe.R
import com.example.android.tunesvibe.viewmodel.MainViewModel

@Composable
fun MainScreen(navController: NavHostController) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val audiosList by mainViewModel.audiosList.collectAsState()
    val lazyListState = rememberLazyListState()
    Scaffold { innerPadding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(innerPadding)
        ) {
            itemsIndexed(audiosList) { index, audio ->

                AudioItem(
                    index = index+1,
                    title = audio.title,
                    artist = audio.artist,
                    albumUri = audio.albumArtUri
                ) {
                    navController.navigate(PlaybackScreenRoute(
                        title = audio.title,
                        artist = audio.artist,
                        data = audio.data,
                        albumArtString = audio.albumArtUri.toString()
                    ))
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
    albumUri:Uri?,
    onItemSelected: ()->Unit
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = artist,
                fontSize = 18.sp
            )
        }
    }
}