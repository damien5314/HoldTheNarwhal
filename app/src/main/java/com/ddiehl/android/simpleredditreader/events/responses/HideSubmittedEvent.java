package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.RedditLink;

public class HideSubmittedEvent {
    private RedditLink mRedditLink;
    private boolean mToHide;
    private Exception mError;
    private boolean mFailed = false;

    public HideSubmittedEvent(RedditLink link, boolean toHide) {
        mRedditLink = link;
        mToHide = toHide;
    }

    public HideSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public boolean isToHide() {
        return mToHide;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
