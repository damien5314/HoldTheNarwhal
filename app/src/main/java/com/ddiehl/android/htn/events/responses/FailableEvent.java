package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class FailableEvent {

    private Throwable mError;
    private boolean mFailed = false;

    public FailableEvent() { }

    public FailableEvent(@NonNull Throwable error) {
        mError = error;
        mFailed = true;
    }

    @Nullable
    public Throwable getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
