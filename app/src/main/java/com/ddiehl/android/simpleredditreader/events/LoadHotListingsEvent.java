package com.ddiehl.android.simpleredditreader.events;

/**
 * Created by Damien on 1/19/2015.
 */
public class LoadHotListingsEvent {

    private String mSubreddit;

    public LoadHotListingsEvent(String subreddit) {
        mSubreddit = subreddit;
    }

    public String getSubreddit() {
        return mSubreddit;
    }
}
