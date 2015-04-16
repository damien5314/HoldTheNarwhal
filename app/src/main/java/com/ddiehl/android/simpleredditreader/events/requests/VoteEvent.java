package com.ddiehl.android.simpleredditreader.events.requests;

public class VoteEvent {
    private String mType;
    private String mId;
    private int mDirection;

    public VoteEvent(String type, String id, int dir) {
        mType = type;
        mId = id;
        mDirection = dir;
    }

    public String getType() {
        return mType;
    }

    public String getId() {
        return mId;
    }

    public int getDirection() {
        return mDirection;
    }
}
