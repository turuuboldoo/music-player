package mn.turbo.spotify.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import mn.turbo.spotify.common.Resource
import mn.turbo.spotify.data.entity.Song
import mn.turbo.spotify.player.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicConnection: MusicServiceConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicConnection.isConnected
    val networkError = musicConnection.networkError
    val playbackState = musicConnection.playbackState
    val currentPlayingSong = musicConnection.currentPlayingSong

    init {
        _mediaItems.postValue(Resource.Loading())

        musicConnection.subscribe(MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {
                override fun onChildrenLoaded(
                    parentId: String,
                    children: MutableList<MediaBrowserCompat.MediaItem>
                ) {
                    super.onChildrenLoaded(parentId, children)
                    val items = children.map {
                        Song(
                            it.mediaId!!,
                            it.description.title.toString(),
                            it.description.subtitle.toString(),
                            it.description.mediaUri.toString(),
                            it.description.iconUri.toString()
                        )
                    }
                    _mediaItems.postValue(Resource.Success(items))
                }
            })
    }

    fun skipToNext() = musicConnection.transportControls.skipToNext()

    fun skipToPrev() = musicConnection.transportControls.skipToPrevious()

    fun seekTo(position: Long) = musicConnection.transportControls.seekTo(position)

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared &&
            mediaItem.mediaId == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)
        ) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> {
                        if (toggle) musicConnection.transportControls.pause()
                    }
                    playbackState.isPlayEnabled -> {
                        musicConnection.transportControls.play()
                    }
                    else -> Unit
                }
            }
        } else {
            musicConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicConnection.unsubscribe(
            MEDIA_ROOT_ID,
            object : MediaBrowserCompat.SubscriptionCallback() {}
        )
    }
}