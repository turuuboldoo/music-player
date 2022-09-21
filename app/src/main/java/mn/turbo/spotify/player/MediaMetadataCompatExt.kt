package mn.turbo.spotify.player

import android.support.v4.media.MediaMetadataCompat
import mn.turbo.spotify.data.entity.Song

fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}