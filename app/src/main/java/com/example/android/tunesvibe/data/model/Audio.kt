package com.example.android.tunesvibe.data.model

import android.net.Uri

data class Audio(
    val id: Int,
    val title: String,
    val artist: String,
    val albumArtUri: Uri?,
    val data: String
)
