package com.ddiehl.android.simpleredditreader.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.CommentBank;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.presenter.CommentsPresenter;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LINK = 0;
    private static final int TYPE_COMMENT = 1;
    private static final int TYPE_COMMENT_STUB = 2;

    private CommentsPresenter mCommentsPresenter;

//    private RedditLink mRedditLink;
//    private CommentBank mCommentBank;

    public CommentsAdapter(CommentsPresenter presenter) {
        mCommentsPresenter = presenter;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_LINK;

        AbsRedditComment comment = mCommentsPresenter.getCommentAtPosition(position - 1);

        if (comment instanceof RedditComment)
            return TYPE_COMMENT;

        return TYPE_COMMENT_STUB;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LINK:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.link_item, parent, false);
                return new LinkViewHolder(view, mCommentsPresenter);
            case TYPE_COMMENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
//                return new CommentViewHolder(mRedditLink, view);
                return new CommentViewHolder(view, mCommentsPresenter);
            case TYPE_COMMENT_STUB:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comment_item, parent, false);
                return new CommentStubViewHolder(view, mCommentsPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LinkViewHolder) {
            RedditLink link = mCommentsPresenter.getRedditLink();
            ((LinkViewHolder) holder).bind(link);
        } else if (holder instanceof CommentViewHolder) {
//            RedditComment comment = (RedditComment) mCommentBank.getVisibleComment(position - 1);
            RedditComment comment = (RedditComment) mCommentsPresenter.getCommentAtPosition(position - 1);
            ((CommentViewHolder) holder).bind(comment);
        } else if (holder instanceof CommentStubViewHolder) {
//            RedditMoreComments comment = (RedditMoreComments) mCommentBank.getVisibleComment(position - 1);
            RedditMoreComments comment = (RedditMoreComments) mCommentsPresenter.getCommentAtPosition(position - 1);
            ((CommentStubViewHolder) holder).bind(comment);
        }
    }

    @Override
    public int getItemCount() {
//        if (mCommentBank == null)
//            return 0;
//
//        // Add 1 for each header and footer view
//        return mCommentBank.getNumVisible() + 1;
        return mCommentsPresenter.getNumComments();
    }

    public void setRedditLink(RedditLink link) {
//        mRedditLink = link;
        notifyItemChanged(0);
    }

    public void setCommentBank(CommentBank bank) {
//        mCommentBank = bank;
        notifyDataSetChanged();
    }
}
