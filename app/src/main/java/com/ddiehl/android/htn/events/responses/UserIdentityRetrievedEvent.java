package com.ddiehl.android.htn.events.responses;


import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentityRetrievedEvent extends FailableEvent {

    private UserIdentity mUserIdentity;

    public UserIdentityRetrievedEvent(UserIdentity response) {
        mUserIdentity = response;
    }

    public UserIdentityRetrievedEvent(Throwable error) {
        super(error);
    }

    @Nullable
    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
