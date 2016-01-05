package com.ddiehl.android.htn.view.viewholders;

import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.reddit.listings.Link;

public class CommentsLinkViewHolder extends AbsLinkViewHolder {
  public CommentsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v, presenter);
  }

  @Override
  protected void showLiked(Link link) {
    int color;
    if (link.isLiked() == null) {
      color = ContextCompat.getColor(mContext, R.color.secondary_text);
    } else if (link.isLiked()) {
      color = ContextCompat.getColor(mContext, R.color.reddit_orange_full);
    } else {
      color = ContextCompat.getColor(mContext, R.color.reddit_blue_full);
    }
    Integer score = link.getScore();
    int length = score == 0 ? 0 : (int) Math.log10(score) + 1;
    Spannable s = new SpannableString(mLinkScore.getText());
    int index = mLinkScore.getText().toString().indexOf(score.toString());
    s.setSpan(new ForegroundColorSpan(color), index, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    mLinkScore.setText(s);
  }
}
