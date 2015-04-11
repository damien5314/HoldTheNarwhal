package com.ddiehl.android.simpleredditreader.events;

import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;

public class UserIdentitySavedEvent {

    private UserIdentity mId;

    public UserIdentitySavedEvent(UserIdentity id) {
        mId = id;
    }

    public UserIdentity getUserIdentity() {
        return mId;
    }
}
