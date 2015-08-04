/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.identity.Trophy;

import java.util.List;

public class TrophiesLoadedEvent extends FailableEvent {

    private List<Trophy> mTrophies;

    public TrophiesLoadedEvent(List<Trophy> trophies) {
        mTrophies = trophies;
    }

    public TrophiesLoadedEvent(Throwable error) {
        super(error);
    }

    public List<Trophy> getTrophies() {
        return mTrophies;
    }
}
