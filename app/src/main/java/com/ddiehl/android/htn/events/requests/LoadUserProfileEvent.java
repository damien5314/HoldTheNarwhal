package com.ddiehl.android.htn.events.requests;

public class LoadUserProfileEvent {

    private String mShow;
    private String mUsername;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadUserProfileEvent(String show, String username, String sort, String timespan, String after) {
        mShow = show;
        mUsername = username;
        mSort = sort;
        mTimeSpan = timespan;
        mAfter = after;
    }

    public String getShow() {
        return mShow;
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