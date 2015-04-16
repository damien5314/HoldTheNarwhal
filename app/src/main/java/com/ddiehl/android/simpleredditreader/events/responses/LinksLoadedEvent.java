package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Damien on 1/19/2015.
 */
public class LinksLoadedEvent {
    private ListingResponse mResponse;
    private List<RedditLink> mLinks;
    private RetrofitError mError;
    private boolean mFailed = false;

    public LinksLoadedEvent(ListingResponse response) {
        mResponse = response;
        mLinks = response.getData().getChildren();
    }

    public LinksLoadedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public ListingResponse getResponse() {
        return mResponse;
    }

    public List<RedditLink> getLinks() {
        return mLinks;
    }

    public RetrofitError getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
