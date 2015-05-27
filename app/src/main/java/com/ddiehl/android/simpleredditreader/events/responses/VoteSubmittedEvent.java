package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.Votable;

public class VoteSubmittedEvent extends FailableEvent {
    private Votable mListing;
    private int mDirection;

    public VoteSubmittedEvent(Votable v, int direction) {
        mListing = v;
        mDirection = direction;
    }

    public VoteSubmittedEvent(Exception e) {
        super(e);
    }

    public Votable getListing() {
        return mListing;
    }

    public int getDirection() {
        return mDirection;
    }
}
