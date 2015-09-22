package com.ddiehl.android.htn.events.responses;

public class FriendDeletedEvent extends FailableEvent {

    private String mUsername;

    public FriendDeletedEvent(String username) {
        mUsername = username;
    }

    public FriendDeletedEvent(Throwable error) {
        super(error);
    }

    public String getUsername() {
        return mUsername;
    }
}
