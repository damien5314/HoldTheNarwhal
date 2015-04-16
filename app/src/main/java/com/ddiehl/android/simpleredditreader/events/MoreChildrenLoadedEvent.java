package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.MoreChildrenResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;

import java.util.List;

import retrofit.RetrofitError;

public class MoreChildrenLoadedEvent {
    private RedditMoreComments mParentStub;
    private List<AbsRedditComment> mComments;
    private RetrofitError mError;
    private boolean mFailed = false;

    public MoreChildrenLoadedEvent(RedditMoreComments parentStub, MoreChildrenResponse moreChildrenResponse) {
        mParentStub = parentStub;
        mComments = moreChildrenResponse.getChildComments();
    }

    public MoreChildrenLoadedEvent(RetrofitError error) {
        mError = error;
        mFailed = true;
    }

    public RedditMoreComments getParentStub() {
        return mParentStub;
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
