package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;

import java.util.List;


public class ListingsLoadedEvent extends FailableEvent {
    private ListingResponse mResponse;
    private List<Listing> mListings;

    public ListingsLoadedEvent(@NonNull ListingResponse response) {
        mResponse = response;
        mListings = response.getData().getChildren();
    }

    public ListingsLoadedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public ListingResponse getResponse() {
        return mResponse;
    }

    @Nullable
    public List<Listing> getListings() {
        return mListings;
    }
}
