package com.ddiehl.android.htn.identity;

import rxreddit.model.UserIdentity;

public interface IdentityManager {

    UserIdentity getUserIdentity();

    void saveUserIdentity(UserIdentity identity);

    void clearSavedUserIdentity();

    void registerUserIdentityChangeListener(Callbacks listener);

    void unregisterUserIdentityChangeListener(Callbacks listener);

    interface Callbacks {

        void onUserIdentityChanged(UserIdentity identity);
    }
}
