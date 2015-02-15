package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingsResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListing;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damien on 1/19/2015.
 */
public class ListingsLoadedEvent {
    List<RedditListing.RedditListingData> mListings;

    public ListingsLoadedEvent(ListingsResponse response) {
        mListings = new ArrayList<>();

        List<RedditListing> listings = response.getData().getChildren();

        for (RedditListing listing : listings) {
            mListings.add(listing.getData());
        }
    }

    public List<RedditListing.RedditListingData> getListings() {
        return mListings;
    }
}
