package com.ddiehl.android.htn.listings.comments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;
import com.ddiehl.android.htn.view.viewholders.BaseLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.CommentsLinkViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadCommentViewHolder;
import com.ddiehl.android.htn.view.viewholders.ThreadStubViewHolder;

import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Link;
import rxreddit.model.Listing;

public class LinkCommentsAdapter extends ListingsAdapter {

    private static final int TYPE_LINK = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_COMMENT_STUB = 2;

    private final LinkCommentsView mLinkCommentsView;
    private final LinkCommentsPresenter mLinkCommentsPresenter;

    public LinkCommentsAdapter(LinkCommentsView linkCommentsView, LinkCommentsPresenter presenter) {
        super(presenter, linkCommentsView, linkCommentsView, null);
        mLinkCommentsView = linkCommentsView;
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
                return new CommentsLinkViewHolder(view, mLinkCommentsView, mLinkCommentsPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_comments_comment, parent, false);
                return new ThreadCommentViewHolder(view, mLinkCommentsView, mLinkCommentsPresenter);
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
            boolean showParentLink = mLinkCommentsPresenter.shouldShowParentLink();
            ((BaseLinkViewHolder) holder).bind(link, true, mode, showNsfw, showParentLink);
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
