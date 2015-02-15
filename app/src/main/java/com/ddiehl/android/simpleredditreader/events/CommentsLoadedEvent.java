package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.redditapi.listings.Listing;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditLink;

import java.util.ArrayList;
import java.util.List;


public class CommentsLoadedEvent {
    private RedditLink.RedditLinkData mLink;
    private List<RedditComment> mComments;

    public CommentsLoadedEvent(List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink.RedditLinkData) linkResponse.getData().getChildren().get(0).getData();

        List<Listing> commentListings = commentsResponse.getData().getChildren();
        mComments = new ArrayList<>();
        for (Listing listing : commentListings) {
            mComments.add((RedditComment) listing);
        }
    }

    public RedditLink.RedditLinkData getLink() {
        return mLink;
    }

    public List<RedditComment> getComments() {
        return mComments;
    }
}