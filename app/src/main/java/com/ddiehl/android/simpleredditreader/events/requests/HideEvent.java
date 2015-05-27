package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.Hideable;

public class HideEvent {
    private Hideable mListing;
    private boolean mToHide;

    public HideEvent(Hideable listing, boolean save) {
        mListing = listing;
        mToHide = save;
    }

    public Hideable getListing() {
        return mListing;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
