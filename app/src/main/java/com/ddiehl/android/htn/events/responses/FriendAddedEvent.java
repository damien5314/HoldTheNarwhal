package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class FriendAddedEvent extends FailableEvent {

    private String mUsername;
    private String mNote;

    public FriendAddedEvent(@NonNull String username, @Nullable String note) {
        mUsername = username;
        mNote = note;
    }

    public FriendAddedEvent(Throwable error) {
        super(error);
    }

    @Nullable
    public String getUsername() {
        return mUsername;
    }

    @Nullable
    public String getNote() {
        return mNote;
    }
}
