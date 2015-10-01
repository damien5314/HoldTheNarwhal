package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FriendDeletedEvent extends FailableEvent {

    private String mUsername;

    public FriendDeletedEvent(@NonNull String username) {
        mUsername = username;
    }

    public FriendDeletedEvent(@NonNull Throwable error) {
        super(error);
    }

    @Nullable
    public String getUsername() {
        return mUsername;
    }
}
