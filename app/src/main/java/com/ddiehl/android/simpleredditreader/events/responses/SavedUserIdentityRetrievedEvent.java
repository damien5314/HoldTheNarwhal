package com.ddiehl.android.simpleredditreader.events.responses;


import com.ddiehl.reddit.identity.UserIdentity;

public class SavedUserIdentityRetrievedEvent {

    private UserIdentity mId;

    public SavedUserIdentityRetrievedEvent(UserIdentity id) {
        mId = id;
    }

    public UserIdentity getUserIdentity() {
        return mId;
    }
}
