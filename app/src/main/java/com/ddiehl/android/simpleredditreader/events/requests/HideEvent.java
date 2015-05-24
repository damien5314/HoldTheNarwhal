package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.listings.RedditLink;

public class HideEvent {
    private RedditLink mRedditLink;
    private boolean mToHide;

    public HideEvent(RedditLink link, boolean save) {
        mRedditLink = link;
        mToHide = save;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
