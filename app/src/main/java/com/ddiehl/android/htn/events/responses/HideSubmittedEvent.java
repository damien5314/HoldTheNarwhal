package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.Hideable;

public class HideSubmittedEvent extends FailableEvent {
    private Hideable mListing;
    private boolean mToHide;

    public HideSubmittedEvent(@NonNull Hideable listing, boolean toHide) {
        mListing = listing;
        mToHide = toHide;
    }

    public HideSubmittedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public Hideable getListing() {
        return mListing;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
