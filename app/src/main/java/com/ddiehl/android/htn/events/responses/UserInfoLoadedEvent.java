package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.UserIdentity;

public class UserInfoLoadedEvent extends FailableEvent {
    private UserIdentity mUserIdentity;

    public UserInfoLoadedEvent(@NonNull UserIdentity id) {
        mUserIdentity = id;
    }

    public UserInfoLoadedEvent(@NonNull Throwable error) {
        super(error);
    }

    @Nullable
    public UserIdentity getUserIdentity() {
        return mUserIdentity;
    }
}
