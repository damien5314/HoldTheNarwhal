package com.ddiehl.android.simpleredditreader.events;

import retrofit.RetrofitError;

public class VoteSubmittedEvent {
    private String mId;
    private int mDirection;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public VoteSubmittedEvent(String id, int direction) {
        mId = id;
        mDirection = direction;
    }

    public VoteSubmittedEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public int getDirection() {
        return mDirection;
    }

    public RetrofitError getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
