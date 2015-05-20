package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.RetrofitError;

public class ReportSubmittedEvent {
    private String mId;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public ReportSubmittedEvent(String id) {
        mId = id;
    }

    public ReportSubmittedEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public RetrofitError getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
