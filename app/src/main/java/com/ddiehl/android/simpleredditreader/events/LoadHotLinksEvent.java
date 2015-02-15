package com.ddiehl.android.simpleredditreader.events;

/**
 * Created by Damien on 1/19/2015.
 */
public class LoadHotLinksEvent {

    private String mSubreddit;

    public LoadHotLinksEvent(String subreddit) {
        mSubreddit = subreddit;
    }

    public String getSubreddit() {
        return mSubreddit;
    }
}
