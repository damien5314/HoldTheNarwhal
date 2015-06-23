package com.ddiehl.android.htn.events.requests;

import com.ddiehl.reddit.Votable;

public class VoteEvent {
    private Votable mListing;
    private String mType;
    private int mDirection;

    public VoteEvent(Votable v, String type, int dir) {
        mListing = v;
        mType = type;
        mDirection = dir;
    }

    public Votable getListing() {
        return mListing;
    }

    public String getType() {
        return mType;
    }

    public int getDirection() {
        return mDirection;
    }
}
