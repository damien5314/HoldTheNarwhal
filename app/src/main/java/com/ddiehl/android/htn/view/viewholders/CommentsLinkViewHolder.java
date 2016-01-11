package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.reddit.listings.Link;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CommentsLinkViewHolder extends AbsLinkViewHolder {
  public CommentsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v, presenter);
  }

  @Override
  protected void showLiked(@NonNull Link link) {
    int color;
    if (link.isLiked() == null) {
      color = ContextCompat.getColor(mContext, R.color.secondary_text);
    } else if (link.isLiked()) {
      color = ContextCompat.getColor(mContext, R.color.reddit_orange_full);
    } else {
      color = ContextCompat.getColor(mContext, R.color.reddit_blue_full);
    }
    Integer score = link.getScore();
    int length = BaseUtils.getNumberOfDigits(score);
    Spannable s = new SpannableString(mLinkScore.getText());
    int index = mLinkScore.getText().toString().indexOf(score.toString());
    s.setSpan(new ForegroundColorSpan(color), index, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    mLinkScore.setText(s);
  }

  @Override
  protected void showThumbnail(@NonNull Link link, @NonNull LinkPresenter.ThumbnailMode mode) {
    // Nsfw/ThumbnailMode is inconsequential here, just show the best preview for screen size
    String url = null;
    if (link.getPreviewImages() != null) {
      List<Link.Preview.Image> images = link.getPreviewImages();
      Link.Preview.Image.Res image = images.get(0).getSource();
      url = image.getUrl();
      int height = image.getHeight();
      DisplayMetrics display = mContext.getResources().getDisplayMetrics();
      float scale = display.scaledDensity;
      float maxHeight = mContext.getResources().getDimension(R.dimen.link_image_full_size);
      mLinkThumbnail.getLayoutParams().height = (int) Math.min(maxHeight, height * scale);
      // FIXME Find a preview best for screen size
    }
    if (url == null) url = "";
    switch (url) {
      case "nsfw":
        Picasso.with(mContext)
            .load(R.drawable.ic_nsfw2)
            .into(mLinkThumbnail);
        break;
      case "": case "default": case "self":
        mLinkThumbnail.setVisibility(View.GONE);
        break;
      default:
        loadThumbnail(url);
    }
  }
}
