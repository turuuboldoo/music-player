package mn.turbo.spotify.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mn.turbo.spotify.common.Constants
import mn.turbo.spotify.player.MusicService
import mn.turbo.spotify.player.MusicServiceConnection
import mn.turbo.spotify.player.currentPlaybackPosition
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    musicConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = _currentSongDuration

    private val _playerPosition = MutableLiveData<Long>()
    val playerPosition: LiveData<Long> = _playerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition ?: 0
                if (_playerPosition.value != pos) {
                    _playerPosition.postValue(pos)
                    _currentSongDuration.postValue(MusicService.currentSongDuration)
                }
                delay(Constants.UPDATE_PLAYER_INTERVAL)
            }
        }
    }
}