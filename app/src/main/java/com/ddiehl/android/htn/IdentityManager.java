package com.ddiehl.android.htn;

import com.ddiehl.android.htn.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.reddit.identity.UserIdentity;

public interface IdentityManager {
    UserIdentity getUserIdentity();
    void onUserIdentityRetrieved(UserIdentityRetrievedEvent event);
    void saveUserIdentity(UserIdentity identity);
    void clearSavedUserIdentity();
}
