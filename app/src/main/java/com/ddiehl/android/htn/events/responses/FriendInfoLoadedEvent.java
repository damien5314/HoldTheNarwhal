package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.FriendInfo;

public class FriendInfoLoadedEvent extends FailableEvent {

    private FriendInfo mFriendInfo;

    public FriendInfoLoadedEvent(@NonNull FriendInfo fi) {
        mFriendInfo = fi;
    }

    public FriendInfoLoadedEvent(@NonNull Throwable error) {
        super(error);
    }

    @Nullable
    public FriendInfo getFriendInfo() {
        return mFriendInfo;
    }
}
