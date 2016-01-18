package com.ddiehl.android.htn.view.viewholders;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener {
  private Context mContext = HoldTheNarwhal.getContext();
  private CommentPresenter mCommentPresenter;
  private Comment mComment;

  @Bind(R.id.comment_link_title) TextView mCommentLinkTitleView;
  @Bind(R.id.comment_link_subtitle) TextView mCommentLinkSubtitleView;
  @Bind(R.id.comment_author) TextView mAuthorView;
  @Bind(R.id.comment_score_layout) ViewGroup mScoreViewLayout;
  @Bind(R.id.comment_score) TextView mScoreView;
  @Bind(R.id.comment_timestamp) TimeSinceTextView mTimestampView;
  @Bind(R.id.comment_saved_icon) View mSavedView;
  @Bind(R.id.comment_body) TextView mBodyView;
  @Bind(R.id.comment_gilded_text_view) TextView mGildedText;
  @Bind(R.id.comment_controversiality_indicator) View mControversialityIndicator;

  public ListingsCommentViewHolder(View v, CommentPresenter presenter) {
    super(v);
    mCommentPresenter = presenter;
    ButterKnife.bind(this, v);
    itemView.setOnCreateContextMenuListener(this);
  }

  @OnClick(R.id.comment_metadata)
  void onClickMetadata(View v) {
    v.showContextMenu();
  }

  @OnClick(R.id.comment_body)
  void onClickBody(View v) {
    v.showContextMenu();
  }

  @OnClick(R.id.comment_link_title)
  void onClickTitle() {
    mCommentPresenter.openCommentLink(mComment);
  }

  public void bind(Comment comment, boolean showControversiality) {
    mComment = comment;
    showLinkTitle(comment);
    showLinkSubtitle(comment);
    showAuthor(comment);
    showBody(comment);
    showScore(comment);
    showTimestamp(comment);
    showLiked(comment);
    showSaved(comment);
    showGilded(comment);
    showControversiality(comment, showControversiality);
  }

  private void showLinkTitle(Comment comment) {
    mCommentLinkTitleView.setText(comment.getLinkTitle());
  }

  private void showLinkSubtitle(Comment comment) {
    String linkAuthor = comment.getLinkAuthor();
    String subreddit = comment.getSubreddit();
    String subject = getSubjectAbbreviationForComment(comment);
    if (subject == null) {
      String titleFormatter = mContext.getString(R.string.listing_comment_subtitle_format);
      mCommentLinkSubtitleView.setText(
          String.format(titleFormatter, subreddit, linkAuthor));
    } else {
      String titleFormatter = mContext.getString(R.string.listing_comment_inbox_subtitle_format);
      mCommentLinkSubtitleView.setText(
          String.format(titleFormatter, subreddit, subject));
    }
  }

  private String getSubjectAbbreviationForComment(Comment comment) {
    String subject = comment.getSubject();
    if (subject == null) return null;
    int resId;
    if ("comment reply".equals(subject)) {
      resId = R.string.listing_comment_subject_commentreply;
    } else if ("post reply".equals(subject)) {
      resId = R.string.listing_comment_subject_postreply;
    } else { // if ("username mention".equals(subject)) {
      resId = R.string.listing_comment_subject_usernamemention;
    }
    return mContext.getString(resId);
  }

  private void showAuthor(Comment comment) {
    mAuthorView.setVisibility(View.VISIBLE);
    String authorType = null;
    String distinguished = comment.getDistinguished();
    if (distinguished != null && !distinguished.equals("")) {
      authorType = distinguished;
    }
    if (authorType != null) {
      switch (authorType) {
        case "op":
          mAuthorView.setBackgroundResource(R.drawable.author_op_bg);
          mAuthorView.setTextColor(
              ContextCompat.getColor(mContext, R.color.author_op_text));
          break;
        case "moderator":
          mAuthorView.setBackgroundResource(R.drawable.author_moderator_bg);
          mAuthorView.setTextColor(
              ContextCompat.getColor(mContext, R.color.author_moderator_text));
          break;
        case "admin":
          mAuthorView.setBackgroundResource(R.drawable.author_admin_bg);
          mAuthorView.setTextColor(
              ContextCompat.getColor(mContext, R.color.author_admin_text));
          break;
        default:
      }
    } else {
      mAuthorView.setBackgroundResource(0);
      mAuthorView.setTextColor(
          ContextCompat.getColor(mContext, R.color.secondary_text));
    }
    mAuthorView.setText(comment.getAuthor());
  }

  private void showBody(Comment comment) {
    mBodyView.setText(comment.getBody().trim());
  }

  private void showScore(Comment comment) {
    if (comment.getScore() == null) {
      mScoreViewLayout.setVisibility(View.GONE);
    } else {
      mScoreViewLayout.setVisibility(View.VISIBLE);
      mScoreView.setText(
          String.format(mContext.getString(R.string.comment_score), comment.getScore()));
    }
  }

  private void showTimestamp(Comment comment) {
    mTimestampView.setDate(comment.getCreateUtc().longValue());
    if (comment.isEdited() != null) {
      switch (comment.isEdited()) {
        case "":
        case "0":
        case "false":
          setEdited(false);
          break;
        default:
          setEdited(true);
      }
    }
  }

  private void setEdited(boolean edited) {
    CharSequence text = mTimestampView.getText();
    mTimestampView.setText(edited ? text + "*" : text.toString().replace("*", ""));
  }

  // Set background tint based on isLiked
  private void showLiked(Comment comment) {
    if (comment.isLiked() == null) {
      mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.secondary_text));
    } else if (comment.isLiked()) {
      mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.reddit_orange_full));
    } else {
      mScoreView.setTextColor(ContextCompat.getColor(mContext, R.color.reddit_blue_full));
    }
  }

  private void showSaved(Comment comment) {
    mSavedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);
  }

  private void showGilded(Comment comment) {
    Integer gilded = comment.getGilded();
    if (gilded != null && gilded > 0) {
      mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
      mGildedText.setVisibility(View.VISIBLE);
    } else {
      mGildedText.setVisibility(View.GONE);
    }
  }

  private void showControversiality(Comment comment, boolean showControversiality) {
    mControversialityIndicator.setVisibility(showControversiality
        ? (comment.getControversiality() > 0 ? View.VISIBLE : View.GONE)
        : View.GONE);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    mCommentPresenter.showCommentContextMenu(menu, v, menuInfo, mComment);
  }
}
