package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.RedditLink;

public class VoteSubmittedEvent {
    private RedditLink mRedditLink;
    private int mDirection;
    private Exception mError;
    private boolean mFailed = false;

    public VoteSubmittedEvent(RedditLink link, int direction) {
        mRedditLink = link;
        mDirection = direction;
    }

    public VoteSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public int getDirection() {
        return mDirection;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
