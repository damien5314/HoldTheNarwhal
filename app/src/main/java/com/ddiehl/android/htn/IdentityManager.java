package com.ddiehl.android.htn;

import rx.functions.Action1;
import rxreddit.model.UserIdentity;

public interface IdentityManager {

    UserIdentity getUserIdentity();

    void saveUserIdentity(UserIdentity identity);

    void clearSavedUserIdentity();

    void registerUserIdentityChangeListener(Callbacks listener);

    void unregisterUserIdentityChangeListener(Callbacks listener);

    interface Callbacks {

        Action1<UserIdentity> onUserIdentityChanged();
    }
}
