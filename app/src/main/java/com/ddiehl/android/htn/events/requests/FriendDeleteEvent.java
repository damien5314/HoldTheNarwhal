package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class FriendDeleteEvent {

    private String mUsername;

    public FriendDeleteEvent(@NonNull String username) {
        mUsername = username;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }
}
