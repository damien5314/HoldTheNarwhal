package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

import com.ddiehl.reddit.Hideable;

public class HideEvent {
    private Hideable mListing;
    private boolean mToHide;

    public HideEvent(Hideable listing, boolean save) {
        mListing = listing;
        mToHide = save;
    }

    @NonNull
    public Hideable getListing() {
        return mListing;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
