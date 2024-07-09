package com.example.android.tunesvibe.data.repository

import com.example.android.tunesvibe.data.model.Audio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface AudioRepository {
    fun retrieveSongs(): MutableStateFlow<List<Audio>>
}