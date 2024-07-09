package com.example.android.tunesvibe.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioService : Service(),
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener,
//    MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener,
    MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {

    private val binder = LocalBinder()

    private var mediaPlayer: MediaPlayer? = null
    private var resumePosition: Int? = 0
    private var _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> get() = _duration
    private var _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> get() = _currentPosition

    private val serviceScope = CoroutineScope(SupervisorJob()+Dispatchers.Main)

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
                Log.d("AudioService", "onStartCommand: $songUri")
                if (songUri != null) {
                    playAudio(songUri)
                }
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
        _duration.value = mediaPlayer?.duration?.div(1000)?:0
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
//        mediaPlayer?.setOnSeekCompleteListener(this)
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
            resumePosition = mediaPlayer?.currentPosition
        }
    }

    private fun resumeAudio() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.seekTo(resumePosition ?: 0)
            mediaPlayer?.start()
        }
    }

    private fun startUpdatingCurrentPosition() {
        serviceScope.launch {
            while (mediaPlayer!=null && mediaPlayer?.isPlaying == true) {
                _currentPosition.value = mediaPlayer?.currentPosition?.div(1000) ?: 0
                delay(1000)
            }
        }
    }

    fun seekTo(position:Float) {
        mediaPlayer?.seekTo((position*1000).toInt())
    }

    enum class ACTIONS {
        PLAY,
        PAUSE,
        STOP
    }

}