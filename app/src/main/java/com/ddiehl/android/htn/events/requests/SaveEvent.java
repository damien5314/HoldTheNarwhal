package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.Savable;

public class SaveEvent {
    private Savable mListing;
    private String mCategory;
    private boolean mToSave;

    public SaveEvent(@NonNull Savable link, @Nullable String category, boolean save) {
        mListing = link;
        mCategory = category;
        mToSave = save;
    }

    @NonNull
    public Savable getListing() {
        return mListing;
    }

    @Nullable
    public String getCategory() {
        return mCategory;
    }

    public boolean isToSave() {
        return mToSave;
    }
}
