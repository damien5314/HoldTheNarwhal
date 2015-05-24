package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.listings.RedditLink;

public class SaveEvent {
    private RedditLink mRedditLink;
    private String mCategory;
    private boolean mToSave;

    public SaveEvent(RedditLink link, String category, boolean save) {
        mRedditLink = link;
        mCategory = category;
        mToSave = save;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public String getCategory() {
        return mCategory;
    }

    public boolean isToSave() {
        return mToSave;
    }
}
