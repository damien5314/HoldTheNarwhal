package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class FriendAddEvent {

    private String mUsername;

    public FriendAddEvent(@NonNull String username) {
        mUsername = username;
    }

    @NonNull
    public String getUsername() {
        return mUsername;
    }
}
