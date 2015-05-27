package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.reddit.listings.RedditMoreComments;

public class LoadCommentThreadEvent {

    private RedditLink mLink;
    private RedditMoreComments mMoreComments;
    private String mSort;

    public LoadCommentThreadEvent(RedditLink link, RedditMoreComments moreComments, String sort) {
        mLink = link;
        mMoreComments = moreComments;
        mSort = sort;
    }

    public RedditLink getLink() {
        return mLink;
    }

    public RedditMoreComments getMoreComments() {
        return mMoreComments;
    }

    public String getSort() {
        return mSort;
    }
}