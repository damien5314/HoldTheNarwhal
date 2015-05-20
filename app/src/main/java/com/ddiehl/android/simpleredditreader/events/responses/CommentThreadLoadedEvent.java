package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.RedditLink;

import java.util.List;


public class CommentThreadLoadedEvent {
    private RedditLink mLink;
    private List<AbsRedditComment> mComments;
    private int mParentDepth;
    private Exception mError;
    private boolean mFailed = false;

    public CommentThreadLoadedEvent(List<ListingResponse> listingResponseList, int parentDepth) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
        mParentDepth = parentDepth;
    }

    public CommentThreadLoadedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mLink;
    }

    public List<AbsRedditComment> getComments() {
        return mComments;
    }

    public int getParentDepth() {
        return mParentDepth;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}