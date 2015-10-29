package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LoadUserProfileListingEvent {
    private String mShow;
    private String mUsername;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadUserProfileListingEvent(
            @NonNull String show, @NonNull String username, @Nullable String sort,
            @Nullable String timespan, @Nullable String after) {
        mShow = show;
        mUsername = username;
        mSort = sort;
        mTimeSpan = timespan;
        mAfter = after;
    }

    @NonNull
    public String getShow() {
        return mShow;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }

    @Nullable
    public String getSort() {
        return mSort;
    }

    @Nullable
    public String getTimeSpan() {
        return mTimeSpan;
    }

    @Nullable
    public String getAfter() {
        return mAfter;
    }
}
