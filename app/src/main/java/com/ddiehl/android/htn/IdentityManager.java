package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.UserIdentity;

public interface IdentityManager {
    UserIdentity getUserIdentity();
    void saveUserIdentity(UserIdentity identity);
    void clearSavedUserIdentity();
}
