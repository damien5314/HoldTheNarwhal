package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;

import java.util.ArrayList;
import java.util.List;


public class CommentsLoadedEvent {
    private List<RedditComment> mComments = new ArrayList<>();

    public CommentsLoadedEvent(List<ListingResponse<RedditComment>> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse<RedditComment> commentsResponse = listingResponseList.get(1);

        mComments = commentsResponse.getData().getChildren();
    }

    public List<RedditComment> getComments() {
        return mComments;
    }
}