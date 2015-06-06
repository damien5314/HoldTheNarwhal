package com.ddiehl.android.simpleredditreader.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.view.viewholders.ThreadStubViewHolder;
import com.ddiehl.android.simpleredditreader.view.viewholders.ThreadCommentViewHolder;
import com.ddiehl.android.simpleredditreader.view.viewholders.ListingsLinkViewHolder;
import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.RedditComment;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.presenter.LinkCommentsPresenter;

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

        AbsRedditComment comment = mLinkCommentsPresenter.getComment(position - 1);

        if (comment instanceof RedditComment)
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
            RedditLink link = mLinkCommentsPresenter.getLinkContext();
            ((ListingsLinkViewHolder) holder).bind(link, true);
        } else if (holder instanceof ThreadCommentViewHolder) {
            RedditLink link = mLinkCommentsPresenter.getLinkContext();
            RedditComment comment = (RedditComment) mLinkCommentsPresenter.getComment(position - 1);
            ((ThreadCommentViewHolder) holder).bind(link, comment);
        } else if (holder instanceof ThreadStubViewHolder) {
            RedditMoreComments comment = (RedditMoreComments) mLinkCommentsPresenter.getComment(position - 1);
            ((ThreadStubViewHolder) holder).bind(comment);
        }
    }

    @Override
    public int getItemCount() {
        return mLinkCommentsPresenter.getNumComments() + 1;
    }
}
