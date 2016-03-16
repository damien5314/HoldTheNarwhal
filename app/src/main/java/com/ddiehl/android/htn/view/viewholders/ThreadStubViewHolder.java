package com.ddiehl.android.htn.view.viewholders;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.CommentPresenter;
import com.ddiehl.reddit.listings.CommentStub;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThreadStubViewHolder extends RecyclerView.ViewHolder {
  private CommentPresenter mCommentPresenter;
  private CommentStub mCommentStub;
  private String mSubreddit;
  private String mLinkId;

  @Bind(R.id.comment_more) TextView mMoreCommentsView;

  public ThreadStubViewHolder(View v, CommentPresenter presenter) {
    super(v);
    mCommentPresenter = presenter;
    ButterKnife.bind(this, v);
  }

  public void bind(CommentStub comment, String subreddit, String linkId) {
    mCommentStub = comment;
    mSubreddit = subreddit;
    mLinkId = linkId;
    addPaddingViews(comment);
    setMoreCommentsText(comment);
  }

  // Add padding views to indentation_wrapper based on depth of comment
  private void addPaddingViews(CommentStub comment) {
    Context context = HoldTheNarwhal.getContext();
    int viewMargin = (comment.getDepth() - 2)
        * (int) context.getResources().getDimension(R.dimen.comment_indentation_margin);
    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
    Configuration config = context.getResources().getConfiguration();
    if (Build.VERSION.SDK_INT >= 17
        && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
      params.setMargins(0, 0, viewMargin, 0);
    } else {
      params.setMargins(viewMargin, 0, 0, 0);
    }
  }

  private void setMoreCommentsText(CommentStub comment) {
    int count = comment.getCount();
    Context ctx = HoldTheNarwhal.getContext();
    switch (count) {
      case 0:
        mMoreCommentsView.setText(ctx.getString(R.string.continue_thread));
        break;
      default:
        mMoreCommentsView.setText(
            ctx.getResources().getQuantityString(R.plurals.more_comments, count, count));
    }
  }

  @OnClick(R.id.comment_more)
  void onClick() {
    if (mCommentStub.getCount() == 0) {
      String commentId = mCommentStub.getParentId();
      mCommentPresenter.showCommentThread(mSubreddit, mLinkId, commentId);
    } else {
      mCommentPresenter.getMoreComments(mCommentStub);
    }
  }
}
