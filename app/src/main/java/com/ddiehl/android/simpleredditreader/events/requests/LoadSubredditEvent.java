package com.ddiehl.android.simpleredditreader.events.requests;


public class LoadSubredditEvent {

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadSubredditEvent(String subreddit, String sort, String timespan, String after) {
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
