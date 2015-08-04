/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.TrophyResponse;

import java.util.List;


public class TrophiesLoadedEvent extends FailableEvent {
    private TrophyResponse mResponse;
    private List<Listing> mListings;

    public TrophiesLoadedEvent(TrophyResponse response) {
        mResponse = response;
        mListings = response.getData().getTrophies();
    }

    public TrophiesLoadedEvent(Throwable e) {
        super(e);
    }

    public TrophyResponse getResponse() {
        return mResponse;
    }

    public List<Listing> getListings() {
        return mListings;
    }
}
