/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;

import java.util.List;

public class LoadMoreChildrenEvent {
    private Link mLink;
    private CommentStub mMoreComments;
    private List<String> mChildren;
    private String mSort;

    public LoadMoreChildrenEvent(Link link, CommentStub moreComments, List<String> children, String sort) {
        mLink = link;
        mMoreComments = moreComments;
        mChildren = children;
        mSort = sort;
    }

    public Link getLink() {
        return mLink;
    }

    public CommentStub getParentCommentStub() {
        return mMoreComments;
    }

    public List<String> getChildren() {
        return mChildren;
    }

    public String getSort() {
        return mSort;
    }
}
