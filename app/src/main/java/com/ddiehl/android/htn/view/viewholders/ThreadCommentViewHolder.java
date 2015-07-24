/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;
import com.ddiehl.android.htn.view.widgets.RedditDateTextView;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThreadCommentViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private Comment mComment;

    @Bind(R.id.comment_expander_icon) ImageView mExpanderIcon;
    @Bind(R.id.comment_author) TextView mAuthorView;
    @Bind(R.id.comment_score) TextView mScoreView;
    @Bind(R.id.comment_timestamp) RedditDateTextView mTimestampView;
    @Bind(R.id.comment_saved_icon) View mSavedView;
    @Bind(R.id.comment_body) TextView mBodyView;
    @Bind(R.id.comment_gilded_text_view) TextView mGildedText;

    public ThreadCommentViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.bind(this, v);
        itemView.setOnCreateContextMenuListener(this);
    }

    @OnClick(R.id.comment_metadata)
    void onClickMetadata(View v) {
        LinkCommentsPresenter p = (LinkCommentsPresenter) mCommentPresenter;
        p.toggleThreadVisible(mComment);
    }

    @OnClick(R.id.comment_body)
    void onClickBody(View v) {
        v.showContextMenu();
    }

    public void bind(final Link link, final Comment comment) {
        mComment = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2)
                * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (Build.VERSION.SDK_INT >= 17
                && mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }

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
        mBodyView.setText(comment.getBody().trim());
        mBodyView.setVisibility(View.VISIBLE); // FIXME: Try commenting this out
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

        if (comment.isCollapsed()) {
            mBodyView.setVisibility(View.GONE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
        } else {
            mBodyView.setVisibility(View.VISIBLE);
            mExpanderIcon.setImageResource(R.drawable.ic_thread_collapse);
        }

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

    public void bind(final Comment comment) {
        bind(null, comment);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mCommentPresenter.showCommentContextMenu(menu, v, menuInfo, mComment);
    }
}
