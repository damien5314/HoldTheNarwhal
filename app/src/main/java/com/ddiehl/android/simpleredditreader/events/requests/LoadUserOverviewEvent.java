package com.ddiehl.android.simpleredditreader.events.requests;

public class LoadUserOverviewEvent {

    private String mUsername;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadUserOverviewEvent(String username, String sort, String timespan, String after) {
        mUsername = username;
        mSort = sort;
        mTimeSpan = timespan;
        mAfter = after;
    }

    public String getUsername() {
        return mUsername;
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
