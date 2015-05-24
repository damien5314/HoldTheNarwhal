package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.Savable;

public class SaveEvent {
    private Savable mListing;
    private String mCategory;
    private boolean mToSave;

    public SaveEvent(Savable link, String category, boolean save) {
        mListing = link;
        mCategory = category;
        mToSave = save;
    }

    public Savable getListing() {
        return mListing;
    }

    public String getCategory() {
        return mCategory;
    }

    public boolean isToSave() {
        return mToSave;
    }
}
