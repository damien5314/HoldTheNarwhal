package com.ddiehl.android.simpleredditreader.events.requests;

/**
 * Created by Damien on 1/19/2015.
 */
public class LoadLinksEvent {

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadLinksEvent(String subreddit, String sort, String timespan) {
        mSubreddit = subreddit;
        mSort = sort;
        mTimeSpan = timespan;
    }

    public LoadLinksEvent(String subreddit, String sort, String timespan, String after) {
        mSubreddit = subreddit;
        mSort = sort;
        mTimeSpan = timespan;
        mAfter = after;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getSort() {
        return mSort;
    }

    public String getTimeSpan() {
        return mTimeSpan;
    }

    public String getAfter() {
        return mAfter;
    }
}
