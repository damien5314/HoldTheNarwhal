package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.Subreddit;


public class SubredditInfoLoadedEvent extends FailableEvent {
    private Subreddit mSubreddit;

    public SubredditInfoLoadedEvent(Subreddit response) {
        mSubreddit = response;
    }

    public SubredditInfoLoadedEvent(Throwable e) {
        super(e);
    }

    public Subreddit getSubreddit() {
        return mSubreddit;
    }
}
