package com.ddiehl.android.htn.events.responses;

public abstract class FailableEvent {

    private Throwable mError;
    private boolean mFailed = false;

    public FailableEvent() { }

    public FailableEvent(Throwable error) {
        mError = error;
        mFailed = true;
    }

    public Throwable getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
