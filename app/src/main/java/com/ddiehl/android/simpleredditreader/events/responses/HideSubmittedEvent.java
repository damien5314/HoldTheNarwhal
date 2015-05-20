package com.ddiehl.android.simpleredditreader.events.responses;

public class HideSubmittedEvent {
    private String mId;
    private boolean mToHide;
    private Exception mRetrofitError;
    private boolean mFailed = false;

    public HideSubmittedEvent(String id, boolean toHide) {
        mId = id;
        mToHide = toHide;
    }

    public HideSubmittedEvent(Exception error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public boolean isToHide() {
        return mToHide;
    }

    public Exception getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
