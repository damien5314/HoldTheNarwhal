package com.ddiehl.android.simpleredditreader.events;

/**
 * Created by Damien on 1/19/2015.
 */
public class LoadHotLinksEvent {

    private String mSubreddit;
    private String mAfter;

    public LoadHotLinksEvent(String subreddit) {
        mSubreddit = subreddit;
    }

    public LoadHotLinksEvent(String subreddit, String after) {
        mSubreddit = subreddit;
        mAfter = after;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getAfter() {
        return mAfter;
    }
}
