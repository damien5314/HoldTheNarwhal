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
import com.ddiehl.android.htn.presenter.BaseListingsPresenter;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.timesincetextview.TimeSinceTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Comment;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    @Inject protected Context mAppContext;
    private CommentView mCommentView;
    private BaseListingsPresenter mCommentPresenter;
    private Comment mComment;

    @BindView(R.id.comment_link_title) TextView mCommentLinkTitleView;
    @BindView(R.id.comment_link_subtitle) TextView mCommentLinkSubtitleView;
    @BindView(R.id.comment_author) TextView mAuthorView;
    @BindView(R.id.comment_score_layout) ViewGroup mScoreViewLayout;
    @BindView(R.id.comment_score) TextView mScoreView;
    @BindView(R.id.comment_timestamp) TimeSinceTextView mTimestampView;
    @BindView(R.id.comment_saved_icon) View mSavedView;
    @BindView(R.id.comment_body) TextView mBodyView;
    @BindView(R.id.comment_gilded_text_view) TextView mGildedText;
    @BindView(R.id.comment_controversiality_indicator) View mControversialityIndicator;

    public ListingsCommentViewHolder(View view, CommentView commentView, BaseListingsPresenter presenter) {
        super(view);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        mCommentView = commentView;
        mCommentPresenter = presenter;
        ButterKnife.bind(this, view);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_link_title)
    void onClickTitle() {
        mCommentPresenter.openCommentLink(mComment);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata(View v) {
        v.showContextMenu();
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
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
            String titleFormatter = mAppContext.getString(R.string.listing_comment_subtitle_format);
            mCommentLinkSubtitleView.setText(
                    String.format(titleFormatter, subreddit, linkAuthor));
        } else {
            String titleFormatter = mAppContext.getString(R.string.listing_comment_inbox_subtitle_format);
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
        return mAppContext.getString(resId);
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
                            ContextCompat.getColor(mAppContext, R.color.author_op_text));
                    break;
                case "moderator":
                    mAuthorView.setBackgroundResource(R.drawable.author_moderator_bg);
                    mAuthorView.setTextColor(
                            ContextCompat.getColor(mAppContext, R.color.author_moderator_text));
                    break;
                case "admin":
                    mAuthorView.setBackgroundResource(R.drawable.author_admin_bg);
                    mAuthorView.setTextColor(
                            ContextCompat.getColor(mAppContext, R.color.author_admin_text));
                    break;
                default:
            }
        } else {
            mAuthorView.setBackgroundResource(0);
            mAuthorView.setTextColor(
                    ContextCompat.getColor(mAppContext, R.color.secondary_text));
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
                    String.format(mAppContext.getString(R.string.comment_score), comment.getScore()));
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
            mScoreView.setTextColor(ContextCompat.getColor(mAppContext, R.color.secondary_text));
        } else if (comment.isLiked()) {
            mScoreView.setTextColor(ContextCompat.getColor(mAppContext, R.color.reddit_orange_full));
        } else {
            mScoreView.setTextColor(ContextCompat.getColor(mAppContext, R.color.reddit_blue_full));
        }
    }

    private void showSaved(Comment comment) {
        mSavedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);
    }

    private void showGilded(Comment comment) {
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mAppContext.getString(R.string.link_gilded_text), gilded));
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
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentView.showCommentContextMenu(menu, view, mComment);
    }
}
