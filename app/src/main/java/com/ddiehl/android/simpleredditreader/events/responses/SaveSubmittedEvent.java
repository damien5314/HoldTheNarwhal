package com.ddiehl.android.simpleredditreader.events.responses;

public class SaveSubmittedEvent {
    private String mId;
    private boolean mToSave;
    private Exception mError;
    private boolean mFailed = false;

    public SaveSubmittedEvent(String id, boolean toSave) {
        mId = id;
        mToSave = toSave;
    }

    public SaveSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public String getId() {
        return mId;
    }

    public boolean isToSave() {
        return mToSave;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
