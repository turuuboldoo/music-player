package mn.turbo.spotify.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import mn.turbo.spotify.common.Constants
import mn.turbo.spotify.data.entity.Song

class MusicDatabase {

    private val fireStore = FirebaseFirestore.getInstance()
    private val songCollection = fireStore.collection(Constants.SONG_COLLECTIONS)

    suspend fun getSongs(): List<Song> {
        return try {
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
