package com.example.android.tunesvibe.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.example.android.tunesvibe.R
import com.example.android.tunesvibe.data.model.Audio
import com.example.android.tunesvibe.data.repository.AudioRepository
import com.example.android.tunesvibe.utils.NOTIFICATION_CHANNEL_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@UnstableApi
@AndroidEntryPoint
class AudioService : MediaSessionService() {

    @Inject
    lateinit var audioRepository: AudioRepository

    private val binder = LocalBinder()
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private var _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> get() = _duration

    private var _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> get() = _currentPosition

    private var _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    private var _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> get() = _currentIndex

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        val sessionActivityIntent = packageManager.getLaunchIntentForPackage(packageName).let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        mediaSession = MediaSession
            .Builder(this, player)
            .setSessionActivity(sessionActivityIntent)
            .build()

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            1,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.title ?: "Title"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    return sessionActivityIntent
                }

                override fun getCurrentContentText(player: Player): CharSequence {
                    return player.currentMediaItem?.mediaMetadata?.artist ?: "Artist"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): Bitmap? {
                    val bitmapDrawable = ContextCompat.getDrawable(this@AudioService, R.drawable.music_background) as BitmapDrawable
                    return if(player.currentMediaItem?.mediaMetadata?.artworkUri != null) {
                        null
                    } else {
                        bitmapDrawable.bitmap
                    }
                }
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(
                    notificationId: Int,
                    dismissedByUser: Boolean
                ) {
                    super.onNotificationCancelled(notificationId, dismissedByUser)
                    stopSelf()
                    stopForeground(notificationId)
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    super.onNotificationPosted(notificationId, notification, ongoing)
                    startForeground(notificationId, notification)
                }

            })
            .build()
        val audiosList = audioRepository.retrieveSongs().value
        loadAudioList(audiosList)

        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setUseNextAction(true)
        playerNotificationManager.setUsePreviousAction(true)
        playerNotificationManager.setUseFastForwardAction(true)
        playerNotificationManager.setMediaSessionToken(mediaSession.sessionCompatToken)
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        scope.launch {
                            _duration.value = player.duration / 1000
                            updateAudioStates()
                        }
                    }

                    Player.STATE_IDLE -> {

                    }

                    Player.STATE_BUFFERING -> {

                    }

                    Player.STATE_ENDED -> {
                        restartPlayList()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                scope.launch {
                    delay(500)
                    _duration.value = player.duration / 1000
                }
                scope.launch {
                    _currentIndex.value = player.currentMediaItemIndex
                    _currentPosition.value = 0
                }
            }


        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        playerNotificationManager.setPlayer(null)
        mediaSession.release()
        stopSelf()
        scope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val action = intent?.action
        when (action) {
            ACTIONS.PLAY.toString() -> {
                val audioIndex = intent.getIntExtra("index", -1)
                if (audioIndex != -1) {
                    playAudio(audioIndex)
                }
            }

            ACTIONS.PAUSE.toString() -> pauseAudio()

            ACTIONS.RESUME.toString() -> resumeAudio()

            ACTIONS.SKIP_NEXT.toString() -> skipNext()

            ACTIONS.SKIP_PREVIOUS.toString() -> skipPrevious()
        }
        return START_STICKY
    }

    private fun playAudio(index: Int) {
        if (index == player.currentMediaItemIndex) return
        if (index in 0 until player.mediaItemCount) {
            player.seekTo(index, 0)
            player.play()
        } else {
            Log.e("AudioService", "Invalid index")
        }
        getCurrentIndex()
    }

    private fun loadAudioList(audioList: List<Audio>) {
        val mediaItems = audioList.map {
            createMediaItem(
                it.data,
                it.title,
                it.artist,
                it.albumArtUri
            )
        }
        player.setMediaItems(mediaItems)
        player.prepare()
    }

    private fun createMediaItem(audioUri:String, title:String, artist:String, artWorkUri: Uri?) :MediaItem{
        return MediaItem.Builder()
            .setUri(audioUri)
            .setMediaMetadata(MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setArtworkUri(artWorkUri)
                .build()
            )
            .build()
    }

    private fun pauseAudio() {
        player.pause()
    }

    private fun resumeAudio() {
        player.play()
        updateAudioStates()
    }

    private fun skipNext() {
        getCurrentIndex()
        if (_currentIndex.value == player.mediaItemCount - 1) {
            player.seekTo(0, C.TIME_UNSET)
        } else {
            player.seekToNextMediaItem()
        }
    }

    private fun skipPrevious() {
        getCurrentIndex()
        if (_currentIndex.value == 0) {
            player.seekTo(player.mediaItemCount - 1, C.TIME_UNSET)
        } else {
            player.seekToPreviousMediaItem()

        }
    }

    private fun restartPlayList() {
        player.seekTo(0, C.TIME_UNSET)
        player.play()
    }

    private fun updateAudioStates() {
        scope.launch {
            while (true) {
                _currentPosition.value = player.currentPosition / 1000
                _isPlaying.value = player.isPlaying
                delay(1000)
            }
        }
    }

    fun isPlaying() {
        _isPlaying.value = player.isPlaying
    }

    fun seekTo(value: Float) {
        player.seekTo(value.toLong() * 1000)
        player.play()
        getCurrentIndex()
    }

    private fun getCurrentIndex() {
        _currentIndex.value = player.currentMediaItemIndex
    }

    enum class ACTIONS {
        PLAY, PAUSE, RESUME, SKIP_NEXT, SKIP_PREVIOUS
    }
}