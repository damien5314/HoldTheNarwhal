package com.ddiehl.android.simpleredditreader.events;

public class VoteEvent {
    private String mId;
    private int mDirection;

    public VoteEvent(String id, int dir) {
        mId = id;
        mDirection = dir;
    }

    public String getId() {
        return mId;
    }

    public int getDirection() {
        return mDirection;
    }
}
