package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.InboxPresenter;
import com.ddiehl.android.htn.presenter.LinkPresenter;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.view.viewholders.AbsLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsMessageViewHolder;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.PrivateMessage;

public class ListingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int TYPE_LINK = 0x1;
  private static final int TYPE_COMMENT = 0x2;
  private static final int TYPE_PRIVATE_MESSAGE = 0x3;

  protected ListingsPresenter mListingsPresenter;
  protected boolean mShowNsfwTag;
  protected LinkPresenter.ThumbnailMode mThumbnailMode;

  public ListingsAdapter(ListingsPresenter presenter) {
    mListingsPresenter = presenter;
    getSettings();
  }

  @Override
  public int getItemViewType(int position) {
    Listing listing = mListingsPresenter.getListing(position);
    if (listing instanceof Link) return TYPE_LINK;
    if (listing instanceof Comment) return TYPE_COMMENT;
    if (listing instanceof PrivateMessage) return TYPE_PRIVATE_MESSAGE;
    throw new RuntimeException("Item view type not recognized: " + listing.getClass());
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case TYPE_LINK:
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.listings_link, parent, false);
        return new ListingsLinkViewHolder(view, mListingsPresenter);
      case TYPE_COMMENT:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.listings_comment, parent, false);
        return new ListingsCommentViewHolder(view, mListingsPresenter);
      case TYPE_PRIVATE_MESSAGE:
        view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.listings_message, parent, false);
        return new ListingsMessageViewHolder(view, mListingsPresenter);
      default:
        throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (position % 25 == 0) getSettings();
    if (holder instanceof AbsLinkViewHolder) {
      Link link = (Link) mListingsPresenter.getListing(position);
      ((AbsLinkViewHolder) holder).bind(link, false, mThumbnailMode, mShowNsfwTag);
    } else if (holder instanceof ListingsCommentViewHolder) {
      Comment comment = (Comment) mListingsPresenter.getListing(position);
      boolean showControversiality = mListingsPresenter.getShowControversiality()
          && comment.getControversiality() > 0;
      ((ListingsCommentViewHolder) holder).bind(comment, showControversiality);
    } else if (holder instanceof ListingsMessageViewHolder) {
      PrivateMessage message = (PrivateMessage) mListingsPresenter.getListing(position);
      ((ListingsMessageViewHolder) holder)
          .bind(message, mListingsPresenter instanceof InboxPresenter);
    }
  }

  @Override
  public int getItemCount() {
    return mListingsPresenter.getNumListings();
  }

  private void getSettings() {
    mShowNsfwTag = mListingsPresenter.shouldShowNsfwTag();
    mThumbnailMode = mListingsPresenter.getThumbnailMode();
  }
}
