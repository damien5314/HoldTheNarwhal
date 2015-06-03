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

public class CommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnCreateContextMenuListener {
    private static final String TAG = CommentViewHolder.class.getSimpleName();

    private RedditComment mRedditComment;

    private View mView;
    private View mCommentDataRow;
    private ImageView mExpanderIcon;
    private TextView mAuthorView;
    private TextView mScoreView;
    private RedditDateTextView mTimestampView;
    private View mSavedView;
    private TextView mBodyView;
    private View mGildedView;
    private TextView mGildedText;

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;

    public CommentViewHolder(View v, CommentsPresenter presenter) {
        super(v);
        mView = v;
        mCommentDataRow = v.findViewById(R.id.comment_metadata);
        mExpanderIcon = (ImageView) v.findViewById(R.id.comment_expander_icon);
        mAuthorView = (TextView) v.findViewById(R.id.comment_author);
        mScoreView = (TextView) v.findViewById(R.id.comment_score);
        mTimestampView = (RedditDateTextView) v.findViewById(R.id.comment_timestamp);
        mSavedView = v.findViewById(R.id.comment_saved_icon);
        mBodyView = (TextView) v.findViewById(R.id.comment_body);
        mGildedView = v.findViewById(R.id.gilded_view);
        mGildedText = (TextView) v.findViewById(R.id.comment_gilded_text_view);

        itemView.setOnClickListener(this);
        mCommentDataRow.setOnClickListener(this);
        mBodyView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

        mContext = v.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
    }

    public void bind(final RedditLink link, final RedditComment comment) {
        mRedditComment = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2)
                * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mView.getLayoutParams();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.comment_metadata:
                mCommentsPresenter.toggleThreadVisible(mRedditComment);
                break;
            default:
                v.showContextMenu();
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentsPresenter.showCommentContextMenu(menu, v, menuInfo, mRedditComment);
    }
}
