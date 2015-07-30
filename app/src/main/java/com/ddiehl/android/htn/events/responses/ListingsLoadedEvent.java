/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;

import java.util.List;


public class ListingsLoadedEvent extends FailableEvent {
    private ListingResponse mResponse;
    private List<Listing> mListings;

    public ListingsLoadedEvent(ListingResponse response) {
        mResponse = response;
        mListings = response.getData().getChildren();
    }

    public ListingsLoadedEvent(Throwable e) {
        super(e);
    }

    public ListingResponse getResponse() {
        return mResponse;
    }

    public List<Listing> getListings() {
        return mListings;
    }
}
