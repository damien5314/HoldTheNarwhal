package com.ddiehl.android.simpleredditreader.view.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.view.viewholders.CommentStubViewHolder;
import com.ddiehl.android.simpleredditreader.view.viewholders.CommentViewHolder;
import com.ddiehl.android.simpleredditreader.view.viewholders.LinkViewHolder;
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

        AbsRedditComment comment = mLinkCommentsPresenter.getCommentAtPosition(position - 1);

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
                        .inflate(R.layout.link_item, parent, false);
                return new LinkViewHolder(view, mLinksPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
                return new CommentViewHolder(view, mLinkCommentsPresenter);
            case TYPE_COMMENT_STUB:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_stub_item, parent, false);
                return new CommentStubViewHolder(view, mLinkCommentsPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LinkViewHolder) {
            RedditLink link = mLinkCommentsPresenter.getLinkContext();
            ((LinkViewHolder) holder).bind(link, true);
        } else if (holder instanceof CommentViewHolder) {
            RedditLink link = mLinkCommentsPresenter.getLinkContext();
            RedditComment comment = (RedditComment) mLinkCommentsPresenter.getCommentAtPosition(position - 1);
            ((CommentViewHolder) holder).bind(link, comment);
        } else if (holder instanceof CommentStubViewHolder) {
            RedditMoreComments comment = (RedditMoreComments) mLinkCommentsPresenter.getCommentAtPosition(position - 1);
            ((CommentStubViewHolder) holder).bind(comment);
        }
    }

    @Override
    public int getItemCount() {
        return mLinkCommentsPresenter.getNumComments() + 1;
    }
}
