package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.UserIdentity;

public class UserIdentityUpdatedEvent {
    private UserIdentity mUserIdentity;

    public UserIdentityUpdatedEvent(UserIdentity id) {
        mUserIdentity = id;
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
