/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.AbsRedditComment;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.RedditMoreComments;

import java.util.List;

public class MoreChildrenLoadedEvent extends FailableEvent {
    private RedditMoreComments mParentStub;
    private List<Listing> mComments;

    public MoreChildrenLoadedEvent(RedditMoreComments parentStub, MoreChildrenResponse moreChildrenResponse) {
        mParentStub = parentStub;
        mComments = moreChildrenResponse.getChildComments();
    }

    public MoreChildrenLoadedEvent(Exception e) {
        super(e);
    }

    public RedditMoreComments getParentStub() {
        return mParentStub;
    }

    public List<Listing> getComments() {
        return mComments;
    }
}
