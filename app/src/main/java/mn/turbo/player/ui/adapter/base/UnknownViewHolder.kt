package mn.turbo.player.ui.adapter.base

import com.bumptech.glide.RequestManager
import mn.turbo.player.data.entity.Song
import mn.turbo.player.databinding.UnknownBinding

class UnknownViewHolder(unknownBinding: UnknownBinding) : BaseViewHolder(unknownBinding.root) {
    override fun bindView(
        song: Song,
        glide: RequestManager,
        onItemClickListener: ((Song) -> Unit)?
    ) {
    }
}