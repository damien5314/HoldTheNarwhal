package com.ddiehl.android.simpleredditreader.events;

public class LoadHotCommentsEvent {

    private String mSubreddit;
    private String mArticle;

    public LoadHotCommentsEvent(String subreddit, String article) {
        mSubreddit = subreddit;
        mArticle = article;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getArticle() {
        return mArticle;
    }
}
