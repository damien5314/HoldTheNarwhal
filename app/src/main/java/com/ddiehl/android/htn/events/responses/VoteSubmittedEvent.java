package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.Votable;

public class VoteSubmittedEvent extends FailableEvent {
    private Votable mListing;
    private int mDirection;

    public VoteSubmittedEvent(Votable v, int direction) {
        mListing = v;
        mDirection = direction;
    }

    public VoteSubmittedEvent(Throwable e) {
        super(e);
    }

    public Votable getListing() {
        return mListing;
    }

    public int getDirection() {
        return mDirection;
    }
}
