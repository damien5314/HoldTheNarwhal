/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

public class FriendDeleteEvent {

    private String mUsername;

    public FriendDeleteEvent(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }
}