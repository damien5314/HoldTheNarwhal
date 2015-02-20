package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import java.util.List;


public class CommentsLoadedEvent {
    private RedditLink mLink;
    private List<RedditComment> mComments;

    public CommentsLoadedEvent(List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
    }

    public RedditLink getLink() {
        return mLink;
    }

    public List<RedditComment> getComments() {
        return mComments;
    }
}