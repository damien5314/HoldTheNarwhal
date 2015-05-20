package com.ddiehl.android.simpleredditreader.events.responses;

public class VoteSubmittedEvent {
    private String mId;
    private int mDirection;
    private Exception mError;
    private boolean mFailed = false;

    public VoteSubmittedEvent(String id, int direction) {
        mId = id;
        mDirection = direction;
    }

    public VoteSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
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
