package com.ddiehl.android.simpleredditreader.events.requests;

import com.ddiehl.reddit.listings.RedditLink;

public class VoteEvent {
    private RedditLink mRedditLink;
    private String mType;
    private int mDirection;

    public VoteEvent(RedditLink link, String type, int dir) {
        mRedditLink = link;
        mType = type;
        mDirection = dir;
    }

    public RedditLink getLink() {
        return mRedditLink;
    }

    public String getType() {
        return mType;
    }

    public int getDirection() {
        return mDirection;
    }
}
