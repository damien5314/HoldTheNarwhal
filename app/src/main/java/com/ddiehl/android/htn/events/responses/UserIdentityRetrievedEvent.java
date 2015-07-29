/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;


import com.ddiehl.reddit.identity.UserIdentity;

public class UserIdentityRetrievedEvent extends FailableEvent {
    private UserIdentity mUserIdentity;

    public UserIdentityRetrievedEvent(UserIdentity response) {
        mUserIdentity = response;
    }

    public UserIdentityRetrievedEvent(Throwable error) {
        super(error);
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
