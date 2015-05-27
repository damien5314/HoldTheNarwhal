package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.RedditLink;

import java.util.List;

import retrofit.RetrofitError;


public class LinksLoadedEvent extends FailableEvent {
    private ListingResponse mResponse;
    private List<RedditLink> mLinks;

    public LinksLoadedEvent(ListingResponse response) {
        mResponse = response;
        mLinks = response.getData().getChildren();
    }

    public LinksLoadedEvent(RetrofitError e) {
        super(e);
    }

    public ListingResponse getResponse() {
        return mResponse;
    }

    public List<RedditLink> getLinks() {
        return mLinks;
    }
}
