package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentitySavedEvent {

    private UserIdentity mId;

    public UserIdentitySavedEvent(UserIdentity id) {
        mId = id;
    }

    public UserIdentity getUserIdentity() {
        return mId;
    }
}
