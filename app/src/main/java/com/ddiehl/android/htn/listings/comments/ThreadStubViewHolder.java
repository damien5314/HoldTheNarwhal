package com.ddiehl.android.htn.listings.comments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.CommentStub;

public class ThreadStubViewHolder extends RecyclerView.ViewHolder {

    @Inject protected Context appContext;
    private final BaseListingsPresenter commentPresenter;
    private CommentStub commentStub;
    private String subreddit;
    private String linkId;

    @BindView(R.id.comment_more)
    TextView moreCommentsView;

    public ThreadStubViewHolder(View v, BaseListingsPresenter presenter) {
        super(v);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        commentPresenter = presenter;
        ButterKnife.bind(this, v);
    }

    public void bind(CommentStub comment, String subreddit, String linkId) {
        commentStub = comment;
        this.subreddit = subreddit;
        this.linkId = linkId;
        addPaddingViews(comment);
        setMoreCommentsText(comment);
    }

    // Add padding views to indentation_wrapper based on depth of comment
    private void addPaddingViews(CommentStub comment) {
        int viewMargin = (comment.getDepth() - 2)
                * (int) appContext.getResources().getDimension(R.dimen.comment_indentation_margin);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        Configuration config = appContext.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            params.setMargins(0, 0, viewMargin, 0);
        } else {
            params.setMargins(viewMargin, 0, 0, 0);
        }
    }

    private void setMoreCommentsText(CommentStub comment) {
        int count = comment.getCount();
        switch (count) {
            case 0:
                moreCommentsView.setText(
                        appContext.getString(R.string.continue_thread));
                break;
            default:
                moreCommentsView.setText(
                        appContext.getResources().getQuantityString(R.plurals.more_comments, count, count));
        }
    }

    @OnClick(R.id.comment_more)
    void onClick() {
        if (commentStub.getCount() == 0) {
            String commentId = commentStub.getParentId();
            commentPresenter.showCommentThread(subreddit, linkId, commentId);
        } else {
            commentPresenter.getMoreComments(commentStub);
        }
    }
}
