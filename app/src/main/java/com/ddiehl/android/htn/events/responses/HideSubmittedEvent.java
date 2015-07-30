/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.Hideable;

public class HideSubmittedEvent extends FailableEvent {
    private Hideable mListing;
    private boolean mToHide;

    public HideSubmittedEvent(Hideable listing, boolean toHide) {
        mListing = listing;
        mToHide = toHide;
    }

    public HideSubmittedEvent(Throwable e) {
        super(e);
    }

    public Hideable getListing() {
        return mListing;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
