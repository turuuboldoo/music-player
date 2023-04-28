package mn.turbo.player.ui.adapter.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import mn.turbo.player.R
import mn.turbo.player.common.Constants
import mn.turbo.player.data.entity.Song
import mn.turbo.player.databinding.ListItemBinding
import mn.turbo.player.databinding.SwipeItemBinding
import mn.turbo.player.databinding.UnknownBinding
import mn.turbo.player.ui.adapter.SongAdapter
import mn.turbo.player.ui.adapter.SwipeAdapter

abstract class BaseAdapter(
    private val layout: String
) : ListAdapter<Song, BaseViewHolder>(SongDiffUtil()) {

    override fun getItemViewType(position: Int): Int {
        return when (layout) {
            Constants.LIST -> R.layout.list_item
            Constants.SWIPE -> R.layout.swipe_item
            else -> R.layout.unknown
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            R.layout.list_item -> {
                SongAdapter.SongViewHolder(
                    ListItemBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            R.layout.swipe_item -> {
                SwipeAdapter.SwipeViewHolder(
                    SwipeItemBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> UnknownViewHolder(UnknownBinding.inflate(inflater, parent, false))
        }
    }

    protected var onItemClickListener: ((Song) -> Unit)? = null

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    class SongDiffUtil : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song) =
            oldItem.mediaId == newItem.mediaId

        override fun areContentsTheSame(oldItem: Song, newItem: Song) =
            oldItem.hashCode() == newItem.hashCode()
    }
}