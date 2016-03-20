package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.squareup.picasso.Picasso;

import butterknife.OnClick;
import rxreddit.model.Link;

public class ListingsLinkViewHolder extends BaseLinkViewHolder {
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
      mLinkView.setBackgroundColor(
              ContextCompat.getColor(mContext, R.color.transparent));
    } else if (link.isLiked()) {
      mLinkView.setBackgroundColor(
              ContextCompat.getColor(mContext, R.color.reddit_orange_lighter));
    } else {
      mLinkView.setBackgroundColor(
              ContextCompat.getColor(mContext, R.color.reddit_blue_lighter));
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

  @Override
  protected void showParentLink(@NonNull boolean link) {
    // No-op; parent link does not exist in this ViewHolder
  }
}
