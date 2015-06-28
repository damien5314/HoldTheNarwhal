package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.reddit.listings.RedditMoreComments;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThreadStubViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private RedditMoreComments mRedditMoreComments;

    @Bind(R.id.comment_more) TextView mMoreCommentsView;

    public ThreadStubViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.bind(this, v);
    }

    public void bind(RedditMoreComments comment) {
        mRedditMoreComments = comment;

        // Add padding views to indentation_wrapper based on depth of comment
        int viewMargin = (comment.getDepth() - 2) * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (Build.VERSION.SDK_INT >= 17
                && mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }

        mMoreCommentsView.setVisibility(View.VISIBLE);
        int count = comment.getCount();
        mMoreCommentsView.setText(count == 0 ? mContext.getString(R.string.continue_thread) :
                String.format(mContext.getString(R.string.more_comments), count));
    }

    @OnClick(R.id.comment_more)
    void onClick() {
        if (mRedditMoreComments.getCount() == 0) {
            mCommentPresenter.showCommentThread(null, null, mRedditMoreComments.getParentId());
        } else {
            mCommentPresenter.getMoreChildren(mRedditMoreComments);
        }
    }
}
