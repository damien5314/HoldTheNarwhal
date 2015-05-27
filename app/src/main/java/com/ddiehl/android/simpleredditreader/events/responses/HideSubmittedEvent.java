package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.Hideable;

public class HideSubmittedEvent {
    private Hideable mListing;
    private boolean mToHide;
    private Exception mError;
    private boolean mFailed = false;

    public HideSubmittedEvent(Hideable listing, boolean toHide) {
        mListing = listing;
        mToHide = toHide;
    }

    public HideSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public Hideable getListing() {
        return mListing;
    }

    public boolean isToHide() {
        return mToHide;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
