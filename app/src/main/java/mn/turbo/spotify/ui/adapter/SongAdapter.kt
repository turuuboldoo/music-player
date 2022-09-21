package mn.turbo.spotify.ui.adapter

import com.bumptech.glide.RequestManager
import mn.turbo.spotify.common.Constants
import mn.turbo.spotify.data.entity.Song
import mn.turbo.spotify.databinding.ListItemBinding
import mn.turbo.spotify.ui.adapter.base.BaseAdapter
import mn.turbo.spotify.ui.adapter.base.BaseViewHolder
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseAdapter(Constants.LIST) {

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.let { song ->
            holder.bindView(song, glide, onItemClickListener)
        }
    }

    class SongViewHolder(
        private val binding: ListItemBinding
    ) : BaseViewHolder(binding.root) {

        override fun bindView(
            song: Song,
            glide: RequestManager,
            onItemClickListener: ((Song) -> Unit)?
        ) {
            binding.apply {
                glide.load(song.imageUrl).into(songCover)
                songTitle.text = song.title
                songArtist.text = song.subtitle

                root.setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(song)
                    }
                }
            }
        }
    }
}