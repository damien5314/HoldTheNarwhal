package com.ddiehl.android.htn.events.responses;

public class FriendAddedEvent extends FailableEvent {

    private String mUsername;
    private String mNote;

    public FriendAddedEvent(String username, String note) {
        mUsername = username;
        mNote = note;
    }

    public FriendAddedEvent(Throwable error) {
        super(error);
    }

    public String getUsername() {
        return mUsername;
    }

    public String getNote() {
        return mNote;
    }
}
