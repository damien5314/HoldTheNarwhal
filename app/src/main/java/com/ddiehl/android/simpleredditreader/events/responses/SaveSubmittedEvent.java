package com.ddiehl.android.simpleredditreader.events.responses;

import retrofit.RetrofitError;

public class SaveSubmittedEvent {
    private String mId;
    private boolean mToSave;
    private RetrofitError mRetrofitError;
    private boolean mFailed = false;

    public SaveSubmittedEvent(String id, boolean toSave) {
        mId = id;
        mToSave = toSave;
    }

    public SaveSubmittedEvent(RetrofitError error) {
        mRetrofitError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public boolean isToSave() {
        return mToSave;
    }

    public RetrofitError getError() {
        return mRetrofitError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
