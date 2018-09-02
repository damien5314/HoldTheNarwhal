package com.ddiehl.android.htn.listings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.comments.CommentView;
import com.ddiehl.android.htn.listings.comments.ListingsCommentViewHolder;
import com.ddiehl.android.htn.listings.inbox.InboxPresenter;
import com.ddiehl.android.htn.listings.inbox.ListingsMessageViewHolder;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageView;
import com.ddiehl.android.htn.listings.links.BaseLinkViewHolder;
import com.ddiehl.android.htn.listings.links.LinkView;
import com.ddiehl.android.htn.listings.links.ListingsLinkViewHolder;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;

import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class ListingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LINK = 1;
    private static final int TYPE_COMMENT = 2;
    private static final int TYPE_PRIVATE_MESSAGE = 3;

    protected final LinkView linkView;
    protected final CommentView commentView;
    protected final PrivateMessageView privateMessageView;
    protected final BaseListingsPresenter listingsPresenter;
    protected boolean showNsfwTag;
    protected ThumbnailMode thumbnailMode;

    public ListingsAdapter(
            BaseListingsPresenter presenter,
            LinkView linkView, CommentView commentView, PrivateMessageView pmView) {
        this.listingsPresenter = presenter;
        this.linkView = linkView;
        this.commentView = commentView;
        this.privateMessageView = pmView;
        getSettings();
    }

    @Override
    public int getItemViewType(int position) {
        Listing listing = listingsPresenter.getListingAt(position);
        if (listing instanceof Link) return TYPE_LINK;
        if (listing instanceof Comment) return TYPE_COMMENT;
        if (listing instanceof PrivateMessage) return TYPE_PRIVATE_MESSAGE;
        throw new RuntimeException("Item view type not recognized: " + listing.getFullName());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LINK:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listings_link, parent, false);
                return new ListingsLinkViewHolder(view, linkView, listingsPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listings_comment, parent, false);
                return new ListingsCommentViewHolder(view, commentView, listingsPresenter);
            case TYPE_PRIVATE_MESSAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listings_message, parent, false);
                return new ListingsMessageViewHolder(view, privateMessageView, listingsPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position % 25 == 0) getSettings();
        if (holder instanceof BaseLinkViewHolder) {
            Link link = (Link) listingsPresenter.getListingAt(position);
            ((BaseLinkViewHolder) holder).bind(link, false, thumbnailMode, showNsfwTag, false);
        } else if (holder instanceof ListingsCommentViewHolder) {
            Comment comment = (Comment) listingsPresenter.getListingAt(position);
            boolean showControversiality = listingsPresenter.getShowControversiality()
                    && comment.getControversiality() > 0;
            ((ListingsCommentViewHolder) holder).bind(comment, showControversiality);
        } else if (holder instanceof ListingsMessageViewHolder) {
            PrivateMessage message = (PrivateMessage) listingsPresenter.getListingAt(position);
            ((ListingsMessageViewHolder) holder)
                    .bind(message, listingsPresenter instanceof InboxPresenter);
        }
    }

    @Override
    public int getItemCount() {
        return listingsPresenter.getNumListings();
    }

    protected void getSettings() {
        showNsfwTag = listingsPresenter.shouldShowNsfwTag();
        thumbnailMode = listingsPresenter.getThumbnailMode();
    }
}
