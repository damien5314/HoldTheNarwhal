package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;

import java.util.ArrayList;
import java.util.List;


public class CommentsLoadedEvent {
    private List<Listing> mComments = new ArrayList<>();

    public CommentsLoadedEvent(List<ListingResponse<Listing>> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse<Listing> commentsResponse = listingResponseList.get(1);

        mComments = commentsResponse.getData().getChildren();
    }

    public List<Listing> getListings() {
        return mComments;
    }
}