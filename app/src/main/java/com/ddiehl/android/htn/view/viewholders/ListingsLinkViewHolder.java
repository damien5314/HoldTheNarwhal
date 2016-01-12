package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.NonNull;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.reddit.listings.Link;
import com.squareup.picasso.Picasso;

import butterknife.OnClick;

public class ListingsLinkViewHolder extends AbsLinkViewHolder {
  public ListingsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v, presenter);
  }

  @OnClick(R.id.link_comment_count)
  void showCommentsForLink() {
    mLinkPresenter.showCommentsForLink(mLink);
  }

  @Override
  protected void showLiked(@NonNull Link link) {
    if (link.isLiked() == null) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_bg);
    } else if (link.isLiked()) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_upvoted_bg);
    } else {
      mLinkView.setBackgroundResource(R.drawable.listings_card_downvoted_bg);
    }
  }

  @Override
  protected void showThumbnail(@NonNull Link link, @NonNull LinkPresenter.ThumbnailMode mode) {
    String url = null;
    if (link.getOver18()) {
      if (mode == LinkPresenter.ThumbnailMode.NO_THUMBNAIL) {
        mLinkThumbnail.setVisibility(View.GONE);
      } else {
        mLinkThumbnail.setVisibility(View.VISIBLE);
        if (mode == LinkPresenter.ThumbnailMode.VARIANT) {
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
        mLinkThumbnail.setVisibility(View.VISIBLE);
        Picasso.with(mContext)
            .load(R.drawable.ic_nsfw2)
            .into(mLinkThumbnail);
        break;
      case "": case "default": case "self":
        mLinkThumbnail.setVisibility(View.GONE);
        break;
      default:
        mLinkThumbnail.setVisibility(View.VISIBLE);
        loadThumbnail(url);
    }
  }
}
