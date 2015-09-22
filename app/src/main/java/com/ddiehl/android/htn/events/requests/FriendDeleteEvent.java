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
