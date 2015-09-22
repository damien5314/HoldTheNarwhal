package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.reddit.listings.CommentStub;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThreadStubViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private CommentPresenter mCommentPresenter;
    private CommentStub mCommentStub;

    @Bind(R.id.comment_more) TextView mMoreCommentsView;

    public ThreadStubViewHolder(View v, CommentPresenter presenter) {
        super(v);
        mContext = v.getContext().getApplicationContext();
        mCommentPresenter = presenter;
        ButterKnife.bind(this, v);
    }

    public void bind(CommentStub comment) {
        mCommentStub = comment;
        addPaddingViews(comment);
        setMoreCommentsText(comment);
    }

    // Add padding views to indentation_wrapper based on depth of comment
    private void addPaddingViews(CommentStub comment) {
        int viewMargin = (comment.getDepth() - 2) * (int) mContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        if (Build.VERSION.SDK_INT >= 17
                && mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }
    }

    private void setMoreCommentsText(CommentStub comment) {
        int count = comment.getCount();
        switch (count) {
            case 0:
                mMoreCommentsView.setText(mContext.getString(R.string.continue_thread));
                break;
            case 1:
                mMoreCommentsView.setText(mContext.getString(R.string.more_comments_s));
                break;
            default:
                mMoreCommentsView.setText(String.format(mContext.getString(R.string.more_comments), count));
        }
    }

    @OnClick(R.id.comment_more) @SuppressWarnings("unused")
    void onClick() {
        if (mCommentStub.getCount() == 0) {
            mCommentPresenter.showCommentThread(null, null, mCommentStub.getParentId());
        } else {
            mCommentPresenter.getMoreChildren(mCommentStub);
        }
    }
}
