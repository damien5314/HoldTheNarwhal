/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

public class FriendAddEvent {

    private String mUsername;
    private String mNote;

    public FriendAddEvent(String username, String note) {
        mUsername = username;
        mNote = note;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getNote() {
        return mNote;
    }
}
