/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

import com.ddiehl.reddit.identity.FriendInfo;

public class FriendInfoLoadedEvent extends FailableEvent {

    private FriendInfo mFriendInfo;

    public FriendInfoLoadedEvent(FriendInfo fi) {
        mFriendInfo = fi;
    }

    public FriendInfoLoadedEvent(Throwable error) {
        super(error);
    }

    public FriendInfo getFriendInfo() {
        return mFriendInfo;
    }
}
