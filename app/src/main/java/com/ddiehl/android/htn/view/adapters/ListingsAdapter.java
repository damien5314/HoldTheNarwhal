package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.ThumbnailMode;
import com.ddiehl.android.htn.presenter.BaseListingsPresenter;
import com.ddiehl.android.htn.presenter.InboxPresenter;
import com.ddiehl.android.htn.view.CommentView;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.PrivateMessageView;
import com.ddiehl.android.htn.view.viewholders.BaseLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsMessageViewHolder;

import rxreddit.model.Comment;
import rxreddit.model.Link;
import rxreddit.model.Listing;
import rxreddit.model.PrivateMessage;

public class ListingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LINK = 1;
    private static final int TYPE_COMMENT = 2;
    private static final int TYPE_PRIVATE_MESSAGE = 3;

    protected final LinkView mLinkView;
    protected final CommentView mCommentView;
    protected final PrivateMessageView mPrivateMessageView;
    protected final BaseListingsPresenter mListingsPresenter;
    protected final BaseListingsPresenter mLinkPresenter;
    protected final BaseListingsPresenter mCommentPresenter;
    protected final BaseListingsPresenter mMessagePresenter;
    protected boolean mShowNsfwTag;
    protected ThumbnailMode mThumbnailMode;

    public ListingsAdapter(
            BaseListingsPresenter presenter,
            LinkView linkView, BaseListingsPresenter linkPresenter,
            CommentView commentView, BaseListingsPresenter commentPresenter,
            PrivateMessageView pmView, BaseListingsPresenter messagePresenter) {
        mListingsPresenter = presenter;
        mLinkView = linkView;
        mLinkPresenter = linkPresenter;
        mCommentView = commentView;
        mCommentPresenter = commentPresenter;
        mPrivateMessageView = pmView;
        mMessagePresenter = messagePresenter;
        getSettings();
    }

    @Override
    public int getItemViewType(int position) {
        Listing listing = mListingsPresenter.getListingAt(position);
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
                return new ListingsLinkViewHolder(view, mLinkView, mLinkPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listings_comment, parent, false);
                return new ListingsCommentViewHolder(view, mCommentView, mCommentPresenter);
            case TYPE_PRIVATE_MESSAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listings_message, parent, false);
                return new ListingsMessageViewHolder(view, mPrivateMessageView, mMessagePresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position % 25 == 0) getSettings();
        if (holder instanceof BaseLinkViewHolder) {
            Link link = (Link) mListingsPresenter.getListingAt(position);
            ((BaseLinkViewHolder) holder).bind(link, false, mThumbnailMode, mShowNsfwTag, false);
        } else if (holder instanceof ListingsCommentViewHolder) {
            Comment comment = (Comment) mListingsPresenter.getListingAt(position);
            boolean showControversiality = mListingsPresenter.getShowControversiality()
                    && comment.getControversiality() > 0;
            ((ListingsCommentViewHolder) holder).bind(comment, showControversiality);
        } else if (holder instanceof ListingsMessageViewHolder) {
            PrivateMessage message = (PrivateMessage) mListingsPresenter.getListingAt(position);
            ((ListingsMessageViewHolder) holder)
                    .bind(message, mListingsPresenter instanceof InboxPresenter);
        }
    }

    @Override
    public int getItemCount() {
        return mListingsPresenter.getNumListings();
    }

    protected void getSettings() {
        mShowNsfwTag = mLinkPresenter.shouldShowNsfwTag();
        mThumbnailMode = mLinkPresenter.getThumbnailMode();
    }
}
