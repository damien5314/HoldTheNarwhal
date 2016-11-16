package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.LinkView;

import butterknife.OnClick;
import rxreddit.model.Link;

public class ListingsLinkViewHolder extends BaseLinkViewHolder {

    public ListingsLinkViewHolder(View view, LinkView linkView, BaseListingsPresenter presenter) {
        super(view, linkView, presenter);
    }

    @OnClick(R.id.link_comment_count)
    void showCommentsForLink() {
        mLinkPresenter.showCommentsForLink(mLink);
    }

    @Override
    protected void showLiked(@NonNull Link link) {
        if (link.isLiked() == null) {
            mView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.transparent)
            );
        } else if (link.isLiked()) {
            mView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.reddit_orange_lighter)
            );
        } else {
            mView.setBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.reddit_blue_lighter)
            );
        }
    }

    @Override
    protected void showThumbnail(@NonNull Link link, @NonNull ThumbnailMode mode) {
        String url = null;
        if (link.getOver18()) {
            if (mode == ThumbnailMode.NO_THUMBNAIL) {
                mLinkThumbnail.setVisibility(View.GONE);
            } else {
                mLinkThumbnail.setVisibility(View.VISIBLE);
                if (mode == ThumbnailMode.VARIANT) {
                    url = getPreviewUrl(link);
                } else { // ThumbnailMode.FULL
                    url = link.getThumbnail();
                }
            }
        } else {
            mLinkThumbnail.setVisibility(View.VISIBLE);
            url = link.getThumbnail();
        }

        if (url == null) url = "";

        switch (url) {
            case "nsfw":
                mLinkThumbnail.setVisibility(View.GONE);
                // This doesn't look correct, probably just get rid of it
//        mLinkThumbnail.setVisibility(View.VISIBLE);
//        Picasso.with(mContext)
//            .load(R.drawable.ic_nsfw2)
//            .into(mLinkThumbnail);
                break;
            case "":
            case "default":
            case "self":
                mLinkThumbnail.setVisibility(View.GONE);
                break;
            default:
                mLinkThumbnail.setVisibility(View.VISIBLE);
                loadThumbnail(url);
        }
    }

    @Override
    protected void showParentLink(boolean link) {
        // No-op; parent link does not exist in this ViewHolder
    }
}
