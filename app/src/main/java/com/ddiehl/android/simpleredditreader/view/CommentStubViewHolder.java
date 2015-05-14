package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;

class CommentStubViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private View mCommentData;
    private ImageView mExpanderIcon;
    private TextView mAuthorView;
    private View mSecondaryData;
    private TextView mScoreView;
    private TextView mTimestampView;
    private TextView mMoreCommentsView;
    private TextView mBodyView;

    private Context mContext;
    private CommentsPresenter mCommentsPresenter;

    public CommentStubViewHolder(View view, CommentsPresenter presenter) {
        super(view);
        mView = view;
        mCommentData = view.findViewById(R.id.comment_data_row);
        mExpanderIcon = (ImageView) view.findViewById(R.id.comment_expander_icon);
        mAuthorView = (TextView) view.findViewById(R.id.comment_author);
        mSecondaryData = view.findViewById(R.id.comment_secondary_data);
        mScoreView = (TextView) view.findViewById(R.id.comment_score);
        mTimestampView = (TextView) view.findViewById(R.id.comment_timestamp);
        mMoreCommentsView = (TextView) view.findViewById(R.id.comment_more);
        mBodyView = (TextView) view.findViewById(R.id.comment_body);

        mContext = view.getContext().getApplicationContext();
        mCommentsPresenter = presenter;
    }

    public void bind(final RedditMoreComments comment) {
        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 1) * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mView.getLayoutParams();
        params.setMargins(viewMargin, 0, 0, 0);

        mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
        mAuthorView.setVisibility(View.GONE);
        mSecondaryData.setVisibility(View.GONE);
        mBodyView.setVisibility(View.GONE);
        mMoreCommentsView.setVisibility(View.VISIBLE);
        int count = comment.getCount();
        if (count == 0) { // continue thread
            mMoreCommentsView.setText(mContext.getString(R.string.continue_thread));
        } else { // more comments in current thread
            mMoreCommentsView.setText(String.format(mContext.getString(R.string.more_comments), count));
        }
        mMoreCommentsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCommentsPresenter.getMoreChildren(comment);
            }
        });
    }
}
