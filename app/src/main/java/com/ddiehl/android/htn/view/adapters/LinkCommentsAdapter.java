package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.LinkCommentsPresenter;
import com.ddiehl.android.htn.view.viewholders.BaseLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.CommentsLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadStubViewHolder;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;

public class LinkCommentsAdapter extends ListingsAdapter {
  private static final int TYPE_LINK = 0;
  private static final int TYPE_COMMENT = 1;
  private static final int TYPE_COMMENT_STUB = 2;

  private LinkCommentsPresenter mLinkCommentsPresenter;

  public LinkCommentsAdapter(LinkCommentsPresenter presenter) {
    super(presenter, presenter, presenter, null);
    mLinkCommentsPresenter = presenter;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) return TYPE_LINK;
    Listing comment = mListingsPresenter.getListingAt(position - 1);
    if (comment instanceof Comment) return TYPE_COMMENT;
    else return TYPE_COMMENT_STUB;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_LINK:
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.link_comments_link, parent, false);
        return new CommentsLinkViewHolder(view, mLinkCommentsPresenter);
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
    if (holder instanceof BaseLinkViewHolder) {
      Link link = mLinkCommentsPresenter.getLinkContext();
      ThumbnailMode mode = mLinkCommentsPresenter.getThumbnailMode();
      boolean showNsfw = mLinkCommentsPresenter.shouldShowNsfwTag();
      ((BaseLinkViewHolder) holder).bind(link, true, mode, showNsfw);
    } else if (holder instanceof ThreadCommentViewHolder) {
      Link link = mLinkCommentsPresenter.getLinkContext();
      Comment comment = (Comment) mLinkCommentsPresenter.getListingAt(position - 1);
      boolean showControversiality = mLinkCommentsPresenter.getShowControversiality();
      ((ThreadCommentViewHolder) holder).bind(link, comment, showControversiality);
    } else if (holder instanceof ThreadStubViewHolder) {
      CommentStub comment = (CommentStub) mLinkCommentsPresenter.getListingAt(position - 1);
      Link linkContext = mLinkCommentsPresenter.getLinkContext();
      String subreddit = linkContext.getSubreddit();
      String linkId = linkContext.getId();
      ((ThreadStubViewHolder) holder).bind(comment, subreddit, linkId);
    }
  }

  @Override
  public int getItemCount() {
    return mLinkCommentsPresenter.getNumListings()
        + (mLinkCommentsPresenter.getLinkContext() == null ? 0 : 1);
  }
}
