/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.Savable;

public class SaveSubmittedEvent extends FailableEvent {
    private Savable mListing;
    private String mCategory;
    private boolean mToSave;

    public SaveSubmittedEvent(Savable s, String category, boolean b) {
        mListing = s;
        mCategory = category;
        mToSave = b;
    }

    public SaveSubmittedEvent(Exception e) {
        super(e);
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
