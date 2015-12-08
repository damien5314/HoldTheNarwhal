package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.UserIdentity;

import rx.functions.Action1;

public interface UserIdentityListener {
    Action1<UserIdentity> onUserIdentityChanged();
}
