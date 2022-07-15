package com.ddiehl.android.htn.listings.comments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.links.BaseLinkViewHolder;
import com.ddiehl.android.htn.listings.links.CommentsLinkViewHolder;
import com.ddiehl.android.htn.listings.subreddit.ThumbnailMode;

import rxreddit.model.Comment;
import rxreddit.model.CommentStub;
import rxreddit.model.Link;
import rxreddit.model.Listing;

public class LinkCommentsAdapter extends ListingsAdapter {

    private static final int TYPE_LINK = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_COMMENT_STUB = 2;

    private final LinkCommentsView linkCommentsView;
    private final LinkCommentsPresenter linkCommentsPresenter;

    public LinkCommentsAdapter(LinkCommentsView linkCommentsView, LinkCommentsPresenter presenter) {
        super(presenter, linkCommentsView, null);
        this.linkCommentsView = linkCommentsView;
        this.linkCommentsPresenter = presenter;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_LINK;
        Listing comment = listingsPresenter.getListingAt(position - 1);
        if (comment instanceof Comment) return TYPE_COMMENT;
        else return TYPE_COMMENT_STUB;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LINK:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_comments_link, parent, false);
                return new CommentsLinkViewHolder(view, linkCommentsPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_comments_comment, parent, false);
                return new ThreadCommentViewHolder(view, linkCommentsView, linkCommentsPresenter);
            case TYPE_COMMENT_STUB:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_comments_stub, parent, false);
                return new ThreadStubViewHolder(view, linkCommentsPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof BaseLinkViewHolder) {
            Link link = linkCommentsPresenter.getLinkContext();
            ThumbnailMode mode = linkCommentsPresenter.getThumbnailMode();
            boolean showNsfw = linkCommentsPresenter.shouldShowNsfwTag();
            boolean showParentLink = linkCommentsPresenter.shouldShowParentLink();
            ((BaseLinkViewHolder) holder).bind(link, true, mode, showNsfw, showParentLink);
        } else if (holder instanceof ThreadCommentViewHolder) {
            Link link = linkCommentsPresenter.getLinkContext();
            Comment comment = (Comment) linkCommentsPresenter.getListingAt(position - 1);
            boolean showControversiality = linkCommentsPresenter.getShowControversiality();
            ((ThreadCommentViewHolder) holder).bind(link, comment, showControversiality);
        } else if (holder instanceof ThreadStubViewHolder) {
            CommentStub comment = (CommentStub) linkCommentsPresenter.getListingAt(position - 1);
            Link linkContext = linkCommentsPresenter.getLinkContext();
            String subreddit = linkContext.getSubreddit();
            String linkId = linkContext.getId();
            ((ThreadStubViewHolder) holder).bind(comment, subreddit, linkId);
        }
    }

    @Override
    public int getItemCount() {
        return linkCommentsPresenter.getNumListings()
                + (linkCommentsPresenter.getLinkContext() == null ? 0 : 1);
    }
}
