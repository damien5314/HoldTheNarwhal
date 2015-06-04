package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;
import com.ddiehl.android.simpleredditreader.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;
    private RedditComment mRedditComment;

    @InjectView(R.id.comment_metadata) View mCommentDataRow;
    @InjectView(R.id.comment_expander_icon) ImageView mExpanderIcon;
    @InjectView(R.id.comment_author) TextView mAuthorView;
    @InjectView(R.id.comment_score) TextView mScoreView;
    @InjectView(R.id.comment_timestamp) RedditDateTextView mTimestampView;
    @InjectView(R.id.comment_saved_icon) View mSavedView;
    @InjectView(R.id.comment_body) TextView mBodyView;
    @InjectView(R.id.gilded_view) View mGildedView;
    @InjectView(R.id.comment_gilded_text_view) TextView mGildedText;

    public CommentViewHolder(View v, CommentsPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
        ButterKnife.inject(this, v);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata() {
        mCommentsPresenter.toggleThreadVisible(mRedditComment);
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
    }

    public void bind(final RedditLink link, final RedditComment comment) {
        mRedditComment = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2)
                * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.setMargins(viewMargin, 0, 0, 0);

        mAuthorView.setVisibility(View.VISIBLE);
        String authorType = null;
        String distinguished = comment.getDistinguished();
        if (distinguished != null && !distinguished.equals("")) {
            authorType = distinguished;
        }
        if (link != null && comment.getAuthor().equals(link.getAuthor())) {
            authorType = "op";
        }
        if (authorType != null) {
            switch (authorType) {
                case "op":
                    mAuthorView.setBackgroundResource(R.drawable.author_op_background);
                    mAuthorView.setTextColor(mContext.getResources().getColor(R.color.author_op_text));
                    break;
                case "moderator":
                    mAuthorView.setBackgroundResource(R.drawable.author_moderator_background);
                    mAuthorView.setTextColor(mContext.getResources().getColor(R.color.author_moderator_text));
                    break;
                case "admin":
                    mAuthorView.setBackgroundResource(R.drawable.author_admin_background);
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
        if (comment.isCollapsed()) {
            mBodyView.setVisibility(View.GONE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
        } else {
            mBodyView.setVisibility(View.VISIBLE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_collapse);
        }

        // Set background tint based on isLiked
        if (comment.isLiked() == null) {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_background);
        } else if (comment.isLiked()) {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_background_upvoted);
        } else {
            mExpanderIcon.setBackgroundResource(R.drawable.comment_expander_background_downvoted);
        }

        // Show/hide saved icon for saved comments
        mSavedView.setVisibility(comment.isSaved() ? View.VISIBLE : View.GONE);

        // Show gilding view if appropriate, else hide
        Integer gilded = comment.getGilded();
        if (gilded != null && gilded > 0) {
            mGildedText.setText(String.format(mContext.getString(R.string.link_gilded_text), gilded));
            mGildedView.setVisibility(View.VISIBLE);
        } else {
            mGildedView.setVisibility(View.GONE);
        }
    }

    public void bind(final RedditComment comment) {
        bind(null, comment);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentsPresenter.showCommentContextMenu(menu, v, menuInfo, mRedditComment);
    }
}
