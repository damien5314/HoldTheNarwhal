package com.ddiehl.android.htn.view.viewholders;

import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.reddit.listings.Link;

public class ListingsLinkViewHolder extends AbsLinkViewHolder {
  public ListingsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v, presenter);
  }

  @Override
  protected void showLiked(Link link) {
    if (link.isLiked() == null) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_bg);
    } else if (link.isLiked()) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_upvoted_bg);
    } else {
      mLinkView.setBackgroundResource(R.drawable.listings_card_downvoted_bg);
    }
  }
}
