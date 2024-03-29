package mn.turbo.player.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import mn.turbo.player.common.Constants
import mn.turbo.player.player.callback.MusicPlaybackPreparer
import mn.turbo.player.player.callback.MusicPlayerEventListener
import mn.turbo.player.player.callback.MusicPlayerNotificationListener
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSource.Factory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false
    private var isPlayerInitialized = false

    private var currentPlaying: MediaMetadataCompat? = null

    companion object {
        var currentSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)
            ?.let {
                PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }

        mediaSession = MediaSessionCompat(this, TAG)
            .apply {
                setSessionActivity(activityIntent)
                isActive = true
            }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            currentSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            currentPlaying = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)

        exoPlayer.addListener(musicPlayerEventListener)

        musicNotificationManager.showNotification(exoPlayer)
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currentSongIndex = if (currentPlaying == null) 0 else songs.indexOf(itemToPlay)

        exoPlayer.prepare()
        exoPlayer.setMediaSource(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currentSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultSent = firebaseMusicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItem())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(
                                firebaseMusicSource.songs,
                                firebaseMusicSource.songs[0],
                                false
                            )
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(Constants.NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultSent) {
                    result.detach()
                }
            }
        }
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }
}

private const val TAG = "MusicService"
const val MEDIA_ROOT_ID = "root_id"