package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.RedditMoreComments;

import java.util.List;

import retrofit.RetrofitError;

public class MoreChildrenLoadedEvent extends FailableEvent {
    private RedditMoreComments mParentStub;
    private List<AbsRedditComment> mComments;

    public MoreChildrenLoadedEvent(RedditMoreComments parentStub, MoreChildrenResponse moreChildrenResponse) {
        mParentStub = parentStub;
        mComments = moreChildrenResponse.getChildComments();
    }

    public MoreChildrenLoadedEvent(RetrofitError e) {
        super(e);
    }

    public RedditMoreComments getParentStub() {
        return mParentStub;
    }

    public List<AbsRedditComment> getComments() {
        return mComments;
    }
}
