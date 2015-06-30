/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.RedditLink;

import java.util.List;


public class LinkCommentsLoadedEvent extends FailableEvent {
    private RedditLink mLink;
    private List<Listing> mComments;

    public LinkCommentsLoadedEvent(List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
    }

    public LinkCommentsLoadedEvent(Exception e) {
        super(e);
    }

    public RedditLink getLink() {
        return mLink;
    }

    public List<Listing> getComments() {
        return mComments;
    }
}