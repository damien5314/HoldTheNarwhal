package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;

public class SavedUserIdentityRetrievedEvent {

    private UserIdentity mId;

    public SavedUserIdentityRetrievedEvent(UserIdentity id) {
        mId = id;
    }

    public UserIdentity getUserIdentity() {
        return mId;
    }
}
