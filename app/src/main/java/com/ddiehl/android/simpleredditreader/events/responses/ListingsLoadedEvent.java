package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;

import java.util.List;

import retrofit.RetrofitError;


public class ListingsLoadedEvent extends FailableEvent {
    private ListingResponse mResponse;
    private List<Listing> mListings;

    public ListingsLoadedEvent(ListingResponse response) {
        mResponse = response;
        mListings = response.getData().getChildren();
    }

    public ListingsLoadedEvent(RetrofitError e) {
        super(e);
    }

    public ListingResponse getResponse() {
        return mResponse;
    }

    public List<Listing> getListings() {
        return mListings;
    }
}
