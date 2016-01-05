package com.ddiehl.android.htn.view.viewholders;

import android.view.View;

import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.reddit.listings.Link;

public class CommentsLinkViewHolder extends AbsLinkViewHolder {
  public CommentsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v, presenter);
  }

  @Override
  protected void showLiked(Link link) {

  }
}
