/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.identity.UserIdentity;

public class UserInfoLoadedEvent extends FailableEvent {

    private UserIdentity mUserIdentity;

    public UserInfoLoadedEvent(UserIdentity id) {
        mUserIdentity = id;
    }

    public UserInfoLoadedEvent(Throwable error) {
        super(error);
    }

    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
