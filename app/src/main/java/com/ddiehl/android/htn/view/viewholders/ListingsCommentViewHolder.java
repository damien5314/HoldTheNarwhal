package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.RedditComment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ListingsCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private RedditComment mRedditComment;

    @InjectView(R.id.comment_link_title) TextView mCommentLinkTitleView;
    @InjectView(R.id.comment_expander_icon) ImageView mExpanderIcon;
    @InjectView(R.id.comment_author) TextView mAuthorView;
    @InjectView(R.id.comment_score) TextView mScoreView;
    @InjectView(R.id.comment_timestamp) RedditDateTextView mTimestampView;
    @InjectView(R.id.comment_saved_icon) View mSavedView;
    @InjectView(R.id.comment_body) TextView mBodyView;
    @InjectView(R.id.comment_gilded_text_view) TextView mGildedText;

    public ListingsCommentViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.inject(this, v);
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
        mCommentPresenter.openCommentLink(mRedditComment);
    }

    public void bind(final RedditComment comment) {
        mRedditComment = comment;

        // Add author and subreddit to link title text
        String linkTitle = comment.getLinkTitle();
        String linkAuthor = comment.getLinkAuthor();
        String subreddit = comment.getSubreddit();
        SpannableString str = new SpannableString(String.format(mContext.getString(R.string.listing_comment_title_format),
                linkTitle, linkAuthor, subreddit));
        str.setSpan(new RelativeSizeSpan(0.7f), linkTitle.length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        str.setSpan(new StyleSpan(Typeface.BOLD), linkAuthor.length(), author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mCommentLinkTitleView.setText(str);

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
        mBodyView.setVisibility(View.VISIBLE);
        mAuthorView.setText(comment.getAuthor());
        mScoreView.setText(String.format(mContext.getString(R.string.comment_score), comment.getScore()));
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
        mBodyView.setText(comment.getBody().trim());
        mExpanderIcon.setImageResource(0);

        // Set background tint based on isLiked
        if (comment.isLiked() == null) {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_bg);
        } else if (comment.isLiked()) {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_upvoted_bg);
        } else {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_downvoted_bg);
        }

        // Show/hide saved icon for saved comments
        mSavedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);

        // Show gilding view if appropriate, else hide
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedText.setVisibility(View.VISIBLE);
        } else {
            mGildedText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentPresenter.showCommentContextMenu(menu, v, menuInfo, mRedditComment);
    }
}
