package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;
import com.ddiehl.reddit.listings.RedditMoreComments;

class CommentStubViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private TextView mMoreCommentsView;

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;

    public CommentStubViewHolder(View view, CommentsPresenter presenter) {
        super(view);
        mView = view;
        mMoreCommentsView = (TextView) view.findViewById(R.id.comment_more);

        mContext = view.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
    }

    public void bind(final RedditMoreComments comment) {
        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2) * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mView.getLayoutParams();
        params.setMargins(viewMargin, 0, 0, 0);

        mMoreCommentsView.setVisibility(View.VISIBLE);
        int count = comment.getCount();
        if (count == 0) { // continue thread
            mMoreCommentsView.setText(mContext.getString(R.string.continue_thread));
            mMoreCommentsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCommentsPresenter.navigateToCommentThread(comment);
                    // TODO
                    // Stubs like this are actually permalinks to the comment itself on reddit.com
                    // Once the comments-only view is ready we can add the link here
                }
            });
        } else { // more comments in current thread
            mMoreCommentsView.setText(String.format(mContext.getString(R.string.more_comments), count));
            mMoreCommentsView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCommentsPresenter.showMoreChildren(comment);
                }
            });
        }
    }
}
