package mn.turbo.spotify.ui.adapter.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import mn.turbo.spotify.data.entity.Song

abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bindView(song: Song, glide: RequestManager, onItemClickListener: ((Song) -> Unit)?)
}
