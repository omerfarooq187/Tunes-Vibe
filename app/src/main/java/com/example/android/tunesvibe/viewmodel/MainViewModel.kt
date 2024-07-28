package com.example.android.tunesvibe.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.tunesvibe.data.model.Audio
import com.example.android.tunesvibe.data.repository.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: AudioRepository): ViewModel() {
    private var _audiosList = MutableStateFlow<List<Audio>>(emptyList())
    val audiosList: StateFlow<List<Audio>> get() = _audiosList
    init {
        viewModelScope.launch {
            _audiosList = repository.retrieveSongs()
        }
    }

    fun saveLastAudio(context: Context, index:Int) {
        val sharedPreferences = context.getSharedPreferences("audio index", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putInt("index", index)
            apply()
        }
    }

}