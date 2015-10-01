package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class FriendNoteSaveEvent {

    private String mUsername;
    private String mNote;

    public FriendNoteSaveEvent(@NonNull String username, @NonNull String note) {
        mUsername = username;
        mNote = note;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }

    @NonNull
    public String getNote() {
        return mNote;
    }
}
