package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.Votable;

public class VoteSubmittedEvent extends FailableEvent {
    private Votable mListing;
    private int mDirection;

    public VoteSubmittedEvent(@NonNull Votable v, int direction) {
        mListing = v;
        mDirection = direction;
    }

    public VoteSubmittedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public Votable getListing() {
        return mListing;
    }

    public int getDirection() {
        return mDirection;
    }
}
