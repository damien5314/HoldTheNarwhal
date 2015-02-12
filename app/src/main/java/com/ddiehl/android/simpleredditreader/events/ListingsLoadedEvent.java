package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListing;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damien on 1/19/2015.
 */
public class ListingsLoadedEvent {
    List<RedditListingData> mListings;

    public ListingsLoadedEvent(ListingResponse response) {
        mListings = new ArrayList<>();

        List<RedditListing> listings = response.getData().getChildren();

        for (RedditListing listing : listings) {
            mListings.add(listing.getData());
        }
    }

    public List<RedditListingData> getListings() {
        return mListings;
    }
}
