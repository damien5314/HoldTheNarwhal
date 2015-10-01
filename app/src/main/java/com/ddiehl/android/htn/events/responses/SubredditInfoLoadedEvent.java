package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.Subreddit;


public class SubredditInfoLoadedEvent extends FailableEvent {
    private Subreddit mSubreddit;

    public SubredditInfoLoadedEvent(@NonNull Subreddit response) {
        mSubreddit = response;
    }

    public SubredditInfoLoadedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public Subreddit getSubreddit() {
        return mSubreddit;
    }
}
