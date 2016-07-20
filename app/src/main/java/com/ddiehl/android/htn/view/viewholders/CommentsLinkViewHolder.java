package com.ddiehl.android.htn.view.viewholders;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.LinkView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rxreddit.model.Link;

public class CommentsLinkViewHolder extends BaseLinkViewHolder {

  @BindView(R.id.link_parent_view)
  View mParentLinkView;

  public CommentsLinkViewHolder(View view, LinkView linkView, LinkPresenter presenter) {
    super(view, linkView, presenter);
  }

  @OnClick(R.id.link_comment_count)
  void showCommentsForLink() {
    /* no-op */
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
    String scoreStr = score == null ?
        mContext.getString(R.string.hidden_score_placeholder) : score.toString();
    int length = scoreStr.length();
    int index = mLinkScore.getText().toString().indexOf(scoreStr);
    Spannable s = new SpannableString(mLinkScore.getText());
    s.setSpan(new ForegroundColorSpan(color), index, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    mLinkScore.setText(s);
  }

  @Override
  protected void showThumbnail(@NonNull Link link, @NonNull ThumbnailMode mode) {
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
        mLinkThumbnail.setVisibility(View.GONE);
        // This doesn't look correct, probably just get rid of it
//        mLinkThumbnail.setVisibility(View.VISIBLE);
//        Picasso.with(mContext)
//            .load(R.drawable.ic_nsfw2)
//            .into(mLinkThumbnail);
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
  protected void showParentLink(boolean visible) {
    mParentLinkView.setVisibility(visible ? View.VISIBLE : View.GONE);
    mParentLinkView.setOnClickListener(
        view -> mLinkPresenter.showCommentsForLink(mLink));
  }
}
