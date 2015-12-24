package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.Link;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListingsLinkViewHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener {
  private Context mContext;
  private LinkPresenter mLinkPresenter;
  private Link mLink;
  private int mAuthorTextColor;

  @Bind(R.id.link_view) View mLinkView;
  @Bind(R.id.link_saved_view) View mSavedView;
  @Bind(R.id.link_title) TextView mLinkTitle;
  @Bind(R.id.link_domain) TextView mLinkDomain;
  @Bind(R.id.link_score) TextView mLinkScore;
  @Bind(R.id.link_author) TextView mLinkAuthor;
  @Bind(R.id.link_subreddit) TextView mLinkSubreddit;
  @Bind(R.id.link_comment_count) TextView mLinkComments;
  @Bind(R.id.link_self_text) TextView mSelfText;
  @Bind(R.id.link_nsfw_indicator) TextView mNsfwIndicator;
  @Bind(R.id.link_thumbnail) ImageView mLinkThumbnail;
  @Bind(R.id.link_timestamp) RedditDateTextView mLinkTimestamp;
  @Bind(R.id.link_gilded_text_view) TextView mGildedText;
  @Bind(R.id.link_stickied_view) View mStickiedView;

  public ListingsLinkViewHolder(View v, LinkPresenter presenter) {
    super(v);
    mContext = v.getContext().getApplicationContext();
    mLinkPresenter = presenter;
    ButterKnife.bind(this, v);
    itemView.setOnCreateContextMenuListener(this);
    mAuthorTextColor = mLinkAuthor.getCurrentTextColor();
  }

  @OnClick({ R.id.link_title, R.id.link_thumbnail})
  void openLink() {
    mLinkPresenter.openLink(mLink);
  }

  @OnClick(R.id.link_comment_count)
  void showCommentsForLink() {
    mLinkPresenter.showCommentsForLink(mLink);
  }

  @OnClick(R.id.link_view)
  void showContextMenu(View v) {
    v.showContextMenu();
  }

  public void bind(
      Link link, boolean showSelfText, LinkPresenter.ThumbnailMode mode, boolean showNsfw) {
    mLink = link;
    showSelfText(link, showSelfText);
    showScore(link);
    showTitle(link);
    showAuthor(link);
    showTimestamp(link);
    showSubreddit(link);
    showDomain(link);
    showCommentCount(link);
    showNsfwTag(link.getOver18() && showNsfw);
    showThumbnail(link, mode);
    showLiked(link);
    showGilded(link);
    showSaved(link);
    showStickied(link);
  }

  private void showSelfText(Link link, boolean showSelfText) {
    mLinkView.setVisibility(View.VISIBLE);
    if (link.getSelftext() != null && !link.getSelftext().equals("") && showSelfText) {
      mSelfText.setText(link.getSelftext());
      mSelfText.setVisibility(View.VISIBLE);
    } else {
      mSelfText.setVisibility(View.GONE);
    }
  }

  private void showScore(Link link) {
    mLinkScore.setText(
        String.format(mContext.getString(R.string.link_score), link.getScore()));
  }

  private void showTitle(Link link) {
    mLinkTitle.setText(link.getTitle());
  }

  private void showAuthor(Link link) {
    mLinkAuthor.setText(
        String.format(mContext.getString(R.string.link_author), link.getAuthor()));
    String distinguished = link.getDistinguished();
    if (distinguished == null || distinguished.equals("")) {
      mLinkAuthor.setBackgroundResource(0);
      mLinkAuthor.setTextColor(mAuthorTextColor);
    } else {
      switch (distinguished) {
        case "moderator":
          mLinkAuthor.setBackgroundResource(R.drawable.author_moderator_bg);
          mLinkAuthor.setTextColor(
              mContext.getResources().getColor(R.color.author_moderator_text));
          break;
        case "admin":
          mLinkAuthor.setBackgroundResource(R.drawable.author_admin_bg);
          mLinkAuthor.setTextColor(
              mContext.getResources().getColor(R.color.author_admin_text));
          break;
        default:
      }
    }
  }

  private void showTimestamp(Link link) {
    mLinkTimestamp.setDate(link.getCreatedUtc().longValue());
    if (link.isEdited() != null) {
      switch (link.isEdited()) {
        case "":
        case "0":
        case "false":
          mLinkTimestamp.setEdited(false);
          break;
        default:
          mLinkTimestamp.setEdited(true);
      }
    }
  }

  private void showSubreddit(Link link) {
    mLinkSubreddit.setText(
        String.format(mContext.getString(R.string.link_subreddit), link.getSubreddit()));
  }

  private void showDomain(Link link) {
    mLinkDomain.setText(
        String.format(mContext.getString(R.string.link_domain), link.getDomain()));
  }

  private void showCommentCount(Link link) {
    mLinkComments.setText(
        String.format(mContext.getString(R.string.link_comment_count), link.getNumComments()));
  }

  private String getPreviewUrl(@Nullable List<Link.Preview.Image> images) {
    if (images == null || images.size() == 0) return null;
    Link.Preview.Image imageToDisplay;
    // Retrieve preview image to display
    Link.Preview.Image image = images.get(0);
    Link.Preview.Image.Variants variants = image.getVariants();
    if (variants != null && variants.nsfw != null) {
      imageToDisplay = variants.nsfw;
    } else {
      imageToDisplay = image;
    }
    List<Link.Preview.Image.Res> resolutions = imageToDisplay.getResolutions();
    Link.Preview.Image.Res res =
        resolutions.size() > 0 ? resolutions.get(0) : imageToDisplay.getSource();
    return res.getUrl();
  }

  private void showThumbnail(Link link, LinkPresenter.ThumbnailMode mode) {
    String url = null;

    if (link.getOver18()) {
      if (mode == LinkPresenter.ThumbnailMode.NO_THUMBNAIL) {
        mLinkThumbnail.setVisibility(View.GONE);
      } else {
        mLinkThumbnail.setVisibility(View.VISIBLE);
        if (mode == LinkPresenter.ThumbnailMode.VARIANT) {
          url = getPreviewUrl(link.getPreviewImages());
        } else { // ThumbnailMode.FULL
          url = link.getThumbnail();
        }
      }
    } else {
      url = link.getThumbnail();
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

  private void showNsfwTag(boolean b) {
    mNsfwIndicator.setVisibility(b ? View.VISIBLE : View.GONE);
  }

  private void loadThumbnail(String url) {
    Picasso.with(mContext)
        .load(url)
        .placeholder(R.drawable.ic_thumbnail_placeholder)
        .fit().centerCrop()
        .error(R.drawable.ic_alert_error)
        .into(mLinkThumbnail);
  }

  private void showLiked(Link link) {
    if (link.isLiked() == null) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_bg);
    } else if (link.isLiked()) {
      mLinkView.setBackgroundResource(R.drawable.listings_card_upvoted_bg);
    } else {
      mLinkView.setBackgroundResource(R.drawable.listings_card_downvoted_bg);
    }
  }

  private void showGilded(Link link) {
    Integer gilded = link.getGilded();
    if (gilded != null && gilded > 0) {
      mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
      mGildedText.setVisibility(View.VISIBLE);
    } else {
      mGildedText.setVisibility(View.GONE);
    }
  }

  private void showSaved(Link link) {
    Boolean saved = link.isSaved();
    mSavedView.setVisibility(saved != null && saved ? View.VISIBLE : View.INVISIBLE);
  }

  private void showStickied(Link link) {
    Boolean stickied = link.getStickied();
    mStickiedView.setVisibility(stickied != null && stickied ? View.VISIBLE : View.INVISIBLE);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    mLinkPresenter.showLinkContextMenu(menu, v, menuInfo, mLink);
  }
}
