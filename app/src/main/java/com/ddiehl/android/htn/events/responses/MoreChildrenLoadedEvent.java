/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

public class MoreChildrenLoadedEvent extends FailableEvent {
    private CommentStub mParentStub;
    private List<Listing> mComments;

    public MoreChildrenLoadedEvent(CommentStub parentStub, MoreChildrenResponse moreChildrenResponse) {
        mParentStub = parentStub;
        mComments = moreChildrenResponse.getChildComments();
    }

    public MoreChildrenLoadedEvent(Exception e) {
        super(e);
    }

    public CommentStub getParentStub() {
        return mParentStub;
    }

    public List<Listing> getComments() {
        return mComments;
    }
}
