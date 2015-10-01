package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.Votable;

public class VoteEvent {
    private Votable mListing;
    private String mType;
    private int mDirection;

    public VoteEvent(@NonNull Votable v, @NonNull String type, int dir) {
        mListing = v;
        mType = type;
        mDirection = dir;
    }

    @NonNull
    public Votable getListing() {
        return mListing;
    }

    @NonNull
    public String getType() {
        return mType;
    }

    public int getDirection() {
        return mDirection;
    }
}
