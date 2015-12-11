package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.viewholders.ThreadStubViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsLinkViewHolder;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;

public class LinkCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int TYPE_LINK = 0;
  private static final int TYPE_COMMENT = 1;
  private static final int TYPE_COMMENT_STUB = 2;

  private LinkCommentsPresenter mLinkCommentsPresenter;

  public LinkCommentsAdapter(LinkCommentsPresenter presenter) {
    mLinkCommentsPresenter = presenter;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0)
      return TYPE_LINK;

    AbsComment comment = mLinkCommentsPresenter.getComment(position - 1);

    if (comment instanceof Comment)
      return TYPE_COMMENT;
    else
      return TYPE_COMMENT_STUB;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_LINK:
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.listings_link, parent, false);
        return new ListingsLinkViewHolder(view, mLinkCommentsPresenter);
      case TYPE_COMMENT:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.link_comments_comment, parent, false);
        return new ThreadCommentViewHolder(view, mLinkCommentsPresenter);
      case TYPE_COMMENT_STUB:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.link_comments_stub, parent, false);
        return new ThreadStubViewHolder(view, mLinkCommentsPresenter);
      default:
        throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof ListingsLinkViewHolder) {
      Link link = mLinkCommentsPresenter.getLinkContext();
      ((ListingsLinkViewHolder) holder).bind(link, true);
    } else if (holder instanceof ThreadCommentViewHolder) {
      Link link = mLinkCommentsPresenter.getLinkContext();
      Comment comment = (Comment) mLinkCommentsPresenter.getComment(position - 1);
      boolean showControversiality = mLinkCommentsPresenter.getShowControversiality();
      ((ThreadCommentViewHolder) holder).bind(link, comment, showControversiality);
    } else if (holder instanceof ThreadStubViewHolder) {
      CommentStub comment = (CommentStub) mLinkCommentsPresenter.getComment(position - 1);
      ((ThreadStubViewHolder) holder).bind(comment);
    }
  }

  @Override
  public int getItemCount() {
    return mLinkCommentsPresenter.getNumComments() + (mLinkCommentsPresenter.getLinkContext() == null ? 0 : 1);
  }
}
