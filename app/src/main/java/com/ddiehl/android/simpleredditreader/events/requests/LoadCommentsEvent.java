package com.ddiehl.android.simpleredditreader.events.requests;

public class LoadCommentsEvent {

    private String mSubreddit;
    private String mArticle;
    private String mSort;
    private String mCommentId;

    public LoadCommentsEvent(String subreddit, String article, String sort, String commentId) {
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
