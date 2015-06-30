/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;


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
