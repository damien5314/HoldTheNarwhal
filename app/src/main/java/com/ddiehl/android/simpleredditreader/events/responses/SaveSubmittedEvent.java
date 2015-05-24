package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.Savable;

public class SaveSubmittedEvent {
    private Savable mListing;
    private String mCategory;
    private boolean mToSave;
    private Exception mError;
    private boolean mFailed = false;

    public SaveSubmittedEvent(Savable s, String category, boolean b) {
        mListing = s;
        mCategory = category;
        mToSave = b;
    }

    public SaveSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public Savable getListing() {
        return mListing;
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
