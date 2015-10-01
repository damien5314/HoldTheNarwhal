package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.Savable;

public class SaveSubmittedEvent extends FailableEvent {
    private Savable mListing;
    private String mCategory;
    private boolean mToSave;

    public SaveSubmittedEvent(@NonNull Savable s, @Nullable String category, boolean b) {
        mListing = s;
        mCategory = category;
        mToSave = b;
    }

    public SaveSubmittedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
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
