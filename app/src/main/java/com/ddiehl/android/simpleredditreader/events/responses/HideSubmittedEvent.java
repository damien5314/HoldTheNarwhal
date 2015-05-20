package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.RetrofitError;

public class HideSubmittedEvent {
    private String mId;
    private boolean mToHide;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public HideSubmittedEvent(String id, boolean toHide) {
        mId = id;
        mToHide = toHide;
    }

    public HideSubmittedEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public boolean isToHide() {
        return mToHide;
    }

    public RetrofitError getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
