package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LoadLinkCommentsEvent {

    private String mSubreddit;
    private String mArticle;
    private String mSort;
    private String mCommentId;

    public LoadLinkCommentsEvent(@NonNull String subreddit, @NonNull String article,
                                 @Nullable String sort, @Nullable String commentId) {
        mSubreddit = subreddit;
        mArticle = article;
        mSort = sort;
        mCommentId = commentId;
    }

    @NonNull
    public String getSubreddit() {
        return mSubreddit;
    }

    @NonNull
    public String getArticle() {
        return mArticle;
    }

    @Nullable
    public String getSort() {
        return mSort;
    }

    @Nullable
    public String getCommentId() {
        return mCommentId;
    }
}
