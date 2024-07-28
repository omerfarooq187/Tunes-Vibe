package com.example.android.tunesvibe.utils

import android.content.Context
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.Player.Command
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.example.android.tunesvibe.R
import com.google.common.collect.ImmutableList

@UnstableApi
class AudioNotification(private val context:Context): MediaNotification.Provider {
    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val player = mediaSession.player
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Now Playing")
            .setContentText("Artist - Title")
            .setSmallIcon(R.drawable.music_background)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionCompatToken)
                .setShowActionsInCompactView(0,1,2)
            )

        //Actions for play pause next previous
        notificationBuilder.addAction(
            NotificationCompat.Action(
                R.drawable.baseline_skip_previous_24,
                "Previous",
                actionFactory.createMediaActionPendingIntent(
                    mediaSession,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM.toLong()
                )
            )
        )

        val playPauseBtn = if (player.isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_filled_24
        notificationBuilder.addAction(
            NotificationCompat.Action(
                playPauseBtn,
                "Play Pause",
                actionFactory.createMediaActionPendingIntent(
                    mediaSession,
                    Player.COMMAND_PLAY_PAUSE.toLong()
                )
            )
        )

        notificationBuilder.addAction(
            NotificationCompat.Action(
                R.drawable.baseline_skip_next_24,
                "Next",
                actionFactory.createMediaActionPendingIntent(
                    mediaSession,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM.toLong()
                )
            )
        )

        return MediaNotification(1,notificationBuilder.build())
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean {
        return false
    }


    companion object {
        private const val COMMAND_CODE_SESSION_SKIP_TO_PREVIOUS = 1
        private const val COMMAND_CODE_SESSION_PLAY_PAUSE = 2
        private const val COMMAND_CODE_SESSION_SKIP_TO_NEXT = 3
    }
}