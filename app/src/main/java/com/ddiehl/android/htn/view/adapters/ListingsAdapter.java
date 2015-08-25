/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.view.viewholders.ListingsCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ListingsLinkViewHolder;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.Link;

public class ListingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LINK = 0;
    private static final int TYPE_COMMENT = 1;

    private ListingsPresenter mListingsPresenter;

    public ListingsAdapter(ListingsPresenter presenter) {
        mListingsPresenter = presenter;
    }

    @Override
    public int getItemViewType(int position) {
        Listing listing = mListingsPresenter.getListing(position);

        if (listing instanceof Link) {
            return TYPE_LINK;
        }

        if (listing instanceof Comment) {
            return TYPE_COMMENT;
        }

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
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ListingsLinkViewHolder) {
            Link link = (Link) mListingsPresenter.getListing(position);
            ((ListingsLinkViewHolder) holder).bind(link, false);
        } else if (holder instanceof ListingsCommentViewHolder) {
            Comment comment = (Comment) mListingsPresenter.getListing(position);
            boolean showControversiality = mListingsPresenter.getShowControversiality()
                    && comment.getControversiality() > 0;
            ((ListingsCommentViewHolder) holder).bind(comment, showControversiality);
        }
    }

    @Override
    public int getItemCount() {
        return mListingsPresenter.getNumListings();
    }

}
