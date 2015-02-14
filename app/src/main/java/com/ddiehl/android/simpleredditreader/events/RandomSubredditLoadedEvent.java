package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListing;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditListingData;

import java.util.ArrayList;
import java.util.List;

public class RandomSubredditLoadedEvent {
    List<RedditListingData> mListings;

    public RandomSubredditLoadedEvent(ListingResponse response) {
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
