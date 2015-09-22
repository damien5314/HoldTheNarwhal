package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.Comment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private Comment mComment;

    @Bind(R.id.comment_link_title) TextView mCommentLinkTitleView;
//    @Bind(R.id.comment_expander_icon) ImageView mExpanderIcon;
    @Bind(R.id.comment_author) TextView mAuthorView;
    @Bind(R.id.comment_score) TextView mScoreView;
    @Bind(R.id.comment_timestamp) RedditDateTextView mTimestampView;
    @Bind(R.id.comment_saved_icon) View mSavedView;
    @Bind(R.id.comment_body) TextView mBodyView;
    @Bind(R.id.comment_gilded_text_view) TextView mGildedText;
    @Bind(R.id.comment_controversiality_indicator) View mControversialityIndicator;

    public ListingsCommentViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.bind(this, v);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_metadata) @SuppressWarnings("unused")
    void onClickMetadata(View v) {
        v.showContextMenu();
    }

    @OnClick(R.id.comment_body) @SuppressWarnings("unused")
    void onClickBody(View v) {
        v.showContextMenu();
    }

    @OnClick(R.id.comment_link_title) @SuppressWarnings("unused")
    void onClickTitle() {
        mCommentPresenter.openCommentLink(mComment);
    }

    public void bind(final Comment comment, boolean showControversiality) {
        mComment = comment;
        setLinkTitle(comment);
        showAuthor(comment);
        showBody(comment);
        showScore(comment);
        showTimestamp(comment);
        showExpanderIcon();
        showLiked(comment);
        showSaved(comment);
        showGilded(comment);
        showControversiality(comment, showControversiality);
    }

    // Add author and subreddit to link title text
    private void setLinkTitle(Comment comment) {
        String linkTitle = comment.getLinkTitle();
        String linkAuthor = comment.getLinkAuthor();
        String subreddit = comment.getSubreddit();
        SpannableString str = new SpannableString(String.format(mContext.getString(R.string.listing_comment_title_format),
                linkTitle, linkAuthor, subreddit));
        str.setSpan(new RelativeSizeSpan(0.7f), linkTitle.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        str.setSpan(new StyleSpan(Typeface.BOLD), linkAuthor.length(), author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mCommentLinkTitleView.setText(str);
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
                    mAuthorView.setTextColor(mContext.getResources().getColor(R.color.author_op_text));
                    break;
                case "moderator":
                    mAuthorView.setBackgroundResource(R.drawable.author_moderator_bg);
                    mAuthorView.setTextColor(mContext.getResources().getColor(R.color.author_moderator_text));
                    break;
                case "admin":
                    mAuthorView.setBackgroundResource(R.drawable.author_admin_bg);
                    mAuthorView.setTextColor(mContext.getResources().getColor(R.color.author_admin_text));
                    break;
                default:
            }
        } else {
            mAuthorView.setBackgroundResource(0);
            mAuthorView.setTextColor(mContext.getResources().getColor(R.color.secondary_text));
        }
        mAuthorView.setText(comment.getAuthor());
    }

    private void showBody(Comment comment) {
//        mBodyView.setVisibility(View.VISIBLE);
        mBodyView.setText(comment.getBody().trim());
    }

    private void showScore(Comment comment) {
        mScoreView.setText(String.format(mContext.getString(R.string.comment_score), comment.getScore()));
    }

    private void showTimestamp(Comment comment) {
        mTimestampView.setDate(comment.getCreateUtc().longValue());
        if (comment.isEdited() != null) {
            switch (comment.isEdited()) {
                case "":
                case "0":
                case "false":
                    mTimestampView.setEdited(false);
                    break;
                default:
                    mTimestampView.setEdited(true);
            }
        }
    }

    private void showExpanderIcon() {
//        mExpanderIcon.setImageResource(0);
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
