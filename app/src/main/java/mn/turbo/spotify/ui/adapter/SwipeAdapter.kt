package mn.turbo.spotify.ui.adapter

import com.bumptech.glide.RequestManager
import mn.turbo.spotify.common.Constants
import mn.turbo.spotify.data.entity.Song
import mn.turbo.spotify.databinding.SwipeItemBinding
import mn.turbo.spotify.ui.adapter.base.BaseAdapter
import mn.turbo.spotify.ui.adapter.base.BaseViewHolder
import javax.inject.Inject

class SwipeAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseAdapter(Constants.SWIPE) {

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        getItem(position)?.let { song ->
            holder.bindView(song, glide, onItemClickListener)
        }
    }

    class SwipeViewHolder(
        private val binding: SwipeItemBinding
    ) : BaseViewHolder(binding.root) {

        override fun bindView(
            song: Song,
            glide: RequestManager,
            onItemClickListener: ((Song) -> Unit)?
        ) {
            val text = "${song.title} - ${song.subtitle}"

            binding.apply {
                textViewTitle.text = text
                root.setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(song)
                    }
                }
            }
        }
    }
}