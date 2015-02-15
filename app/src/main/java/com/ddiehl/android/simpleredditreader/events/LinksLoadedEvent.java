package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.listings.Listing;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditLink;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Damien on 1/19/2015.
 */
public class LinksLoadedEvent {
    List<RedditLink> mLinks;

    public LinksLoadedEvent(ListingResponse response) {
        List<Listing> listings = response.getData().getChildren();
        mLinks = new ArrayList<>();
        for (Listing listing : listings) {
            mLinks.add((RedditLink) listing);
        }
    }

    public List<RedditLink> getLinks() {
        return mLinks;
    }
}
