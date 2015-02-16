package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import java.util.List;

/**
 * Created by Damien on 1/19/2015.
 */
public class LinksLoadedEvent {
    private ListingResponse<RedditLink> mResponse;
    private List<RedditLink> mLinks;

    public LinksLoadedEvent(ListingResponse<RedditLink> response) {
        mResponse = response;
        mLinks = response.getData().getChildren();
    }

    public ListingResponse getResponse() {
        return mResponse;
    }

    public List<RedditLink> getLinks() {
        return mLinks;
    }
}
