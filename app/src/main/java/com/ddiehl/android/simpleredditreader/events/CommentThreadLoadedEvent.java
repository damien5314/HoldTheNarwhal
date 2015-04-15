package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import java.util.List;

import retrofit.RetrofitError;


public class CommentThreadLoadedEvent {
    private RedditLink mLink;
    private List<AbsRedditComment> mComments;
    private RetrofitError mError;
    private boolean mFailed = false;

    public CommentThreadLoadedEvent(List<ListingResponse> listingResponseList) {
        // Link is responseList.get(0), comments are responseList.get(1)
        ListingResponse linkResponse = listingResponseList.get(0);
        ListingResponse commentsResponse = listingResponseList.get(1);

        mLink = (RedditLink) linkResponse.getData().getChildren().get(0);
        mComments = commentsResponse.getData().getChildren();
    }

    public CommentThreadLoadedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mLink;
    }

    public List<AbsRedditComment> getComments() {
        return mComments;
    }

    public RetrofitError getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}