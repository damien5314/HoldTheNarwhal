package com.ddiehl.android.htn.events.requests;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LoadSubredditEvent {

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private String mAfter;

    public LoadSubredditEvent(@NonNull String subreddit, @Nullable String sort,
                              @Nullable String timespan, @Nullable String after) {
        mSubreddit = subreddit;
        mSort = sort;
        mTimeSpan = timespan;
        mAfter = after;
    }

    @NonNull
    public String getSubreddit() {
        return mSubreddit;
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
