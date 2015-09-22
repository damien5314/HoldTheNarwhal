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
