/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.requests;

public class LoadUserProfileSummaryEvent {

    private String mUsername;

    public LoadUserProfileSummaryEvent(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }
}
