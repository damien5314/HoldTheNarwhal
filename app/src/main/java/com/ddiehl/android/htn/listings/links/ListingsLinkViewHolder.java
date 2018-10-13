package com.ddiehl.android.htn.listings.links;

import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.utils.ThemeUtilsKt;

import org.jetbrains.annotations.NotNull;

import butterknife.OnClick;
import rxreddit.model.Link;

public class ListingsLinkViewHolder extends BaseLinkViewHolder {

    public ListingsLinkViewHolder(View view, LinkView linkView, BaseListingsPresenter presenter) {
        super(view, linkView, presenter);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        linkPresenter.showCommentsForLink(link);
    }

    @Override
    protected void showLiked(@NotNull Link link) {
        if (link.isLiked() == null) {
            final int likedColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.cardTopBackgroundColor);
            view.setBackgroundColor(likedColor);
        } else if (link.isLiked()) {
            final int likedColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.cardTopBackgroundColorLiked);
            view.setBackgroundColor(likedColor);
        } else {
            final int dislikedColor = ThemeUtilsKt.getColorFromAttr(context, R.attr.cardTopBackgroundColorDisliked);
            view.setBackgroundColor(dislikedColor);
        }
    }

    @Override
    protected void showThumbnail(@NotNull Link link, @NotNull ThumbnailMode mode) {
        String url = null;
        if (link.getOver18()) {
            if (mode == ThumbnailMode.NO_THUMBNAIL) {
                linkThumbnail.setVisibility(View.GONE);
            } else {
                linkThumbnail.setVisibility(View.VISIBLE);
                if (mode == ThumbnailMode.VARIANT) {
                    url = getPreviewUrl(link);
                } else { // ThumbnailMode.FULL
                    url = link.getThumbnail();
                }
            }
        } else {
            linkThumbnail.setVisibility(View.VISIBLE);
            url = link.getThumbnail();
        }

        if (url == null) url = "";

        switch (url) {
            case "nsfw":
                linkThumbnail.setVisibility(View.GONE);
                // This doesn't look correct, probably just get rid of it
//        linkThumbnail.setVisibility(View.VISIBLE);
//        Picasso.with(context)
//            .load(R.drawable.ic_nsfw2)
//            .into(linkThumbnail);
                break;
            case "":
            case "default":
            case "self":
                linkThumbnail.setVisibility(View.GONE);
                break;
            default:
                linkThumbnail.setVisibility(View.VISIBLE);
                loadThumbnail(url);
        }
    }

    @Override
    protected void showParentLink(boolean link) {
        // No-op; parent link does not exist in this ViewHolder
    }
}
