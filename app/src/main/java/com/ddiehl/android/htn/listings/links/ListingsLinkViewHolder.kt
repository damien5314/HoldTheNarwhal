package com.ddiehl.android.htn.listings.links

import android.view.View
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.BaseListingsPresenter
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode
import com.ddiehl.android.htn.utils.getColorFromAttr
import rxreddit.model.Link
import timber.log.Timber

class ListingsLinkViewHolder(
    view: View,
    linkView: LinkView,
    presenter: BaseListingsPresenter,
) : BaseLinkViewHolder(view, linkView, presenter) {

    init {
        linkComments.setOnClickListener {
            showCommentsForLink()
        }
    }

    private fun showCommentsForLink() {
        linkPresenter.showCommentsForLink(link)
    }

    override fun showLiked(link: Link) {
        val color = when (link.liked) {
            true -> getColorFromAttr(context, R.attr.contentPrimaryBackgroundColorLiked)
            false -> getColorFromAttr(context, R.attr.contentPrimaryBackgroundColorDisliked)
            null -> getColorFromAttr(context, R.attr.contentPrimaryBackgroundColor)
        }
        view.setBackgroundColor(color)
    }

    override fun showThumbnail(link: Link, mode: ThumbnailMode) {
        Timber.d("[dcd] showThumbnail: ${mode.name} / ${link.title}")
        if (mode == ThumbnailMode.NO_THUMBNAIL) {
            linkThumbnail.visibility = View.GONE
            return
        }

        // Handle VARIANT and FULL thumbnails
        when (val url = if (link.over18 && mode == ThumbnailMode.VARIANT) getPreviewUrl(link) else link.thumbnail) {
            "nsfw",
            "",
            "default",
            "self",
            null,
            -> linkThumbnail.visibility = View.GONE
            else -> {
                linkThumbnail.visibility = View.VISIBLE
                loadThumbnail(url)
            }
        }
    }

    override fun showParentLink(link: Boolean) {
        // No-op; parent link does not exist in this ViewHolder
    }
}
