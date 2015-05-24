package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.Votable;

public class VoteSubmittedEvent {
    private Votable mListing;
    private int mDirection;
    private Exception mError;
    private boolean mFailed = false;

    public VoteSubmittedEvent(Votable v, int direction) {
        mListing = v;
        mDirection = direction;
    }

    public VoteSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public Votable getListing() {
        return mListing;
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
