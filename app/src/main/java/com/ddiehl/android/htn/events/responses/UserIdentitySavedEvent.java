package com.ddiehl.android.htn.events.responses;


import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentitySavedEvent {
    private UserIdentity mId;

    public UserIdentitySavedEvent(@Nullable UserIdentity id) {
        mId = id;
    }

    @Nullable
    public UserIdentity getUserIdentity() {
        return mId;
    }
}
