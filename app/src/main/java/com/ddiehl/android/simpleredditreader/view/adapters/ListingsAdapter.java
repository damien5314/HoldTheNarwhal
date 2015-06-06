package com.ddiehl.android.simpleredditreader.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.ListingsPresenter;
import com.ddiehl.android.simpleredditreader.view.viewholders.CommentViewHolder;
import com.ddiehl.android.simpleredditreader.view.viewholders.LinkViewHolder;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;

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

        if (listing instanceof RedditLink)
            return TYPE_LINK;

        if (listing instanceof RedditComment)
            return TYPE_COMMENT;

        throw new RuntimeException("Item view type not recognized: " + listing.getClass());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LINK:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_item, parent, false);
                return new LinkViewHolder(view, mListingsPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
                return new CommentViewHolder(view, mListingsPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LinkViewHolder) {
            RedditLink link = (RedditLink) mListingsPresenter.getListing(position);
            ((LinkViewHolder) holder).bind(link, true);
        } else if (holder instanceof CommentViewHolder) {
            RedditLink link = (RedditLink) mListingsPresenter.getListing(position);
            RedditComment comment = (RedditComment) mListingsPresenter.getListing(position);
            ((CommentViewHolder) holder).bind(link, comment);
        }
    }

    @Override
    public int getItemCount() {
        return mListingsPresenter.getNumListings();
    }
}
