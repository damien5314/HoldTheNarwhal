package com.ddiehl.android.htn.listings.links

import android.view.View
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.BaseListingsPresenter
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode
import com.ddiehl.android.htn.utils.getColorFromAttr
import rxreddit.model.Link

class ListingsLinkViewHolder(
    view: View?,
    linkView: LinkView?,
    presenter: BaseListingsPresenter?,
) : BaseLinkViewHolder(view, linkView, presenter) {

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
        var url: String? = null
        if (link.over18) {
            if (mode == ThumbnailMode.NO_THUMBNAIL) {
                linkThumbnail.visibility = View.GONE
            } else {
                linkThumbnail.visibility = View.VISIBLE
                url = if (mode == ThumbnailMode.VARIANT) {
                    getPreviewUrl(link)
                } else { // ThumbnailMode.FULL
                    link.thumbnail
                }
            }
        } else {
            linkThumbnail.visibility = View.VISIBLE
            url = link.thumbnail
        }
        if (url == null) url = ""
        when (url) {
            "nsfw",
            "",
            "default",
            "self",
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

    init {
        linkComments.setOnClickListener { view1: View? -> showCommentsForLink() }
    }
}
