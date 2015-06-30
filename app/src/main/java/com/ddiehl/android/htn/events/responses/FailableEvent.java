/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

public abstract class FailableEvent {

    private Exception mError;
    private boolean mFailed = false;

    public FailableEvent() { }

    public FailableEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
