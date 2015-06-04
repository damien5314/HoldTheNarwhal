package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.Listing;

public class UserOverviewLoaded extends FailableEvent {

    String mUsername;
    Listing mListing;

    public UserOverviewLoaded(String id, Listing listing) {
        mUsername = id;
        mListing = listing;
    }

    public UserOverviewLoaded(Exception error) {
        super(error);
    }

    public String getUsername() {
        return mUsername;
    }

    public Listing getListing() {
        return mListing;
    }
}
