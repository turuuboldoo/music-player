package mn.turbo.spotify.ui.adapter.base

import com.bumptech.glide.RequestManager
import mn.turbo.spotify.data.entity.Song
import mn.turbo.spotify.databinding.UnknownBinding

class UnknownViewHolder(unknownBinding: UnknownBinding) : BaseViewHolder(unknownBinding.root) {
    override fun bindView(
        song: Song,
        glide: RequestManager,
        onItemClickListener: ((Song) -> Unit)?
    ) {
    }
}