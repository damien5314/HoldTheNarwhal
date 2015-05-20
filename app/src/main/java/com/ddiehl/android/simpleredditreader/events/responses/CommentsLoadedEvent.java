package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.RedditLink;

import java.util.List;


public class CommentsLoadedEvent {
    private RedditLink mLink;
    private List<AbsRedditComment> mComments;
    private Exception mError;
    private boolean mFailed = false;

    public CommentsLoadedEvent(List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
    }

    public CommentsLoadedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mLink;
    }

    public List<AbsRedditComment> getComments() {
        return mComments;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}