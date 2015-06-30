/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

public class LoadLinkCommentsEvent {

    private String mSubreddit;
    private String mArticle;
    private String mSort;
    private String mCommentId;

    public LoadLinkCommentsEvent(String subreddit, String article, String sort, String commentId) {
        mSubreddit = subreddit;
        mArticle = article;
        mSort = sort;
        mCommentId = commentId;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getArticle() {
        return mArticle;
    }

    public String getSort() {
        return mSort;
    }

    public String getCommentId() {
        return mCommentId;
    }
}
