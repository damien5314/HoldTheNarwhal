package com.ddiehl.android.simpleredditreader.events.requests;

public class LoadCommentsEvent {

    private String mSubreddit;
    private String mArticle;
    private String mSort;

    public LoadCommentsEvent(String subreddit, String article, String sort) {
        mSubreddit = subreddit;
        mArticle = article;
        mSort = sort;
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
}
