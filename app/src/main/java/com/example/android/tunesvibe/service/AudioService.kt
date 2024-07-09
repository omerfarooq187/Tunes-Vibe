package com.example.android.tunesvibe.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.android.tunesvibe.data.model.Audio
import com.example.android.tunesvibe.data.repository.AudioRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioService : Service(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener,
//    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    @Inject
    lateinit var repository: AudioRepository

//    private val _audiosList = MutableStateFlow<List<Audio>>(emptyList())
//    private val audiosList: StateFlow<List<Audio>> get() = _audiosList

    private val binder = LocalBinder()

    private var mediaPlayer: MediaPlayer? = null
    private var _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration
    private var _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> get() = _currentPosition
    private var _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying
    private var _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> get() = _currentSongIndex
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AudioService", "onCreate:")
        initMediaPlayer()
    }

    init {
        if (mediaPlayer == null) {
            initMediaPlayer()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTIONS.PLAY.toString() -> {
                val songUri = intent.getStringExtra("songUri")
                val index = intent.getIntExtra("index", 0)
                _currentSongIndex.value = index
                if (songUri != null) {
                    playAudio(songUri)
                    _isPlaying.value = true
                }
            }

            ACTIONS.PAUSE.toString() -> {
                pauseAudio()
                _isPlaying.value = false
            }

            ACTIONS.RESUME.toString() -> {
                resumeAudio()
                _isPlaying.value = true
            }

            ACTIONS.STOP.toString() -> {
                stopAudio()
                _isPlaying.value = false
            }

            ACTIONS.SKIP_NEXT.toString() -> {
                skipNext()
                _isPlaying.value = true
            }

            ACTIONS.SKIP_PREVIOUS.toString() -> {
                skipPrevious()
                _isPlaying.value = true
            }
        }
        return START_STICKY
    }

    override fun onCompletion(p0: MediaPlayer?) {
        stopAudio()
        stopSelf()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onPrepared(mediaPlayer: MediaPlayer?) {
        _duration.value = mediaPlayer?.duration?.div(1000) ?: 0
        mediaPlayer?.start()
        startUpdatingCurrentPosition()
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun onAudioFocusChange(p0: Int) {
        TODO("Not yet implemented")
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()

        mediaPlayer?.setOnCompletionListener(this)
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnInfoListener(this)
        mediaPlayer?.setOnBufferingUpdateListener(this)

        mediaPlayer?.reset()
        mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
    }

    private fun playAudio(songUri: String) {
        if (mediaPlayer?.isPlaying == true || (mediaPlayer?.currentPosition ?: 0) > 0) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } else {
            mediaPlayer?.reset()
        }

        mediaPlayer?.apply {
            setDataSource(songUri)
            prepareAsync()
        }
    }

    private fun stopAudio() {
        if (mediaPlayer == null) return
        if (mediaPlayer?.isPlaying == true) mediaPlayer?.stop()
    }

    private fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun resumeAudio() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
        startUpdatingCurrentPosition()
    }

    private fun startUpdatingCurrentPosition() {
        serviceScope.launch {
            while (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                _currentPosition.value = mediaPlayer?.currentPosition?.div(1000) ?: 0
                delay(1000)
            }
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.seekTo((position * 1000).toInt())
    }

    private fun skipNext() {
        val audios = repository.retrieveSongs().value
        if (_currentSongIndex.value < audios.size - 1) {
            _currentSongIndex.value++
            playAudio(audios[_currentSongIndex.value].data)
        } else {
            _currentSongIndex.value = 0
            playAudio(audios[_currentSongIndex.value].data)
        }
    }

    private fun skipPrevious() {
        val audios = repository.retrieveSongs().value
        if (currentSongIndex.value > 0) {
            _currentSongIndex.value--
            playAudio(audios[currentSongIndex.value].data)
        } else {
            _currentSongIndex.value = audios.size - 1
            playAudio(audios[currentSongIndex.value].data)
        }
    }

    enum class ACTIONS {
        PLAY,
        PAUSE,
        RESUME,
        STOP,
        SKIP_NEXT,
        SKIP_PREVIOUS
    }

}