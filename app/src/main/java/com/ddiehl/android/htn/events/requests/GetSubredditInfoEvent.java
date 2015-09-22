package com.ddiehl.android.htn.events.requests;

public class GetSubredditInfoEvent {

    private String mSubredditName;

    public GetSubredditInfoEvent(String name) {
        mSubredditName = name;
    }

    public String getSubredditName() {
        return mSubredditName;
    }
}
