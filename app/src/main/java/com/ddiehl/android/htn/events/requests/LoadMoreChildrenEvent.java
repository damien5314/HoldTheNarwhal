/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

import java.util.List;

public class LoadMoreChildrenEvent {
    private RedditLink mRedditLink;
    private RedditMoreComments mMoreComments;
    private List<String> mChildren;
    private String mSort;

    public LoadMoreChildrenEvent(RedditLink link, RedditMoreComments moreComments, List<String> children, String sort) {
        mRedditLink = link;
        mMoreComments = moreComments;
        mChildren = children;
        mSort = sort;
    }

    public RedditLink getRedditLink() {
        return mRedditLink;
    }

    public RedditMoreComments getParentCommentStub() {
        return mMoreComments;
    }

    public List<String> getChildren() {
        return mChildren;
    }

    public String getSort() {
        return mSort;
    }
}
