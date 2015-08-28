/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

public class FriendAddEvent {

    private String mUsername;

    public FriendAddEvent(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }
}