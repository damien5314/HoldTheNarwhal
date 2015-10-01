package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;

import java.util.List;


public class LinkCommentsLoadedEvent extends FailableEvent {
    private Link mLink;
    private List<Listing> mComments;

    public LinkCommentsLoadedEvent(@NonNull List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (Link) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
    }

    public LinkCommentsLoadedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public Link getLink() {
        return mLink;
    }

    @Nullable
    public List<Listing> getComments() {
        return mComments;
    }
}