package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;

public class LoadMoreCommentsEvent {

    private RedditMoreComments mMoreComments;
    private String mSort;

    public LoadMoreCommentsEvent(RedditMoreComments moreComments, String sort) {
        mMoreComments = moreComments;
        mSort = sort;
    }

    public RedditMoreComments getMoreComments() {
        return mMoreComments;
    }

    public String getSort() {
        return mSort;
    }
}
