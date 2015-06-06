package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.CommentPresenter;
import com.ddiehl.reddit.listings.RedditMoreComments;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class CommentStubViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private RedditMoreComments mRedditMoreComments;

    @InjectView(R.id.comment_more) TextView mMoreCommentsView;

    public CommentStubViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.inject(this, v);
    }

    public void bind(RedditMoreComments comment) {
        mRedditMoreComments = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2) * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        params.setMargins(viewMargin, 0, 0, 0);

        mMoreCommentsView.setVisibility(View.VISIBLE);
        int count = comment.getCount();
        mMoreCommentsView.setText(count == 0 ? mContext.getString(R.string.continue_thread) :
                String.format(mContext.getString(R.string.more_comments), count));
    }

    @OnClick(R.id.comment_more)
    void onClick() {
        if (mRedditMoreComments.getCount() == 0) {
            mCommentPresenter.navigateToCommentThread(mRedditMoreComments.getParentId());
        } else {
            mCommentPresenter.getMoreChildren(mRedditMoreComments);
        }
    }
}
