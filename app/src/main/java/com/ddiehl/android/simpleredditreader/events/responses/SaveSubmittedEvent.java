package com.ddiehl.android.simpleredditreader.events.responses;

import com.ddiehl.reddit.listings.RedditLink;

public class SaveSubmittedEvent {
    private RedditLink mRedditLink;
    private String mCategory;
    private boolean mToSave;
    private Exception mError;
    private boolean mFailed = false;

    public SaveSubmittedEvent(RedditLink link, String category, boolean b) {
        mRedditLink = link;
        mCategory = category;
        mToSave = b;
    }

    public SaveSubmittedEvent(Exception error) {
        mError = error;
        mFailed = true;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public boolean isToSave() {
        return mToSave;
    }

    public Exception getError() {
        return mError;
    }

    public boolean isFailed() {
        return mFailed;
    }
}
