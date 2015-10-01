package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class GetSubredditInfoEvent {

    private String mSubredditName;

    public GetSubredditInfoEvent(@NonNull String name) {
        mSubredditName = name;
    }

    @NonNull
    public String getSubredditName() {
        return mSubredditName;
    }
}
