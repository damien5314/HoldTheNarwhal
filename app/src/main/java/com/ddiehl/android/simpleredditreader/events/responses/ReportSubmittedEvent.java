package com.ddiehl.android.simpleredditreader.events.responses;

public class ReportSubmittedEvent {
    private String mId;
    private Exception mError;
    private boolean mFailed = false;

    public ReportSubmittedEvent(String id) {
        mId = id;
    }

    public ReportSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
