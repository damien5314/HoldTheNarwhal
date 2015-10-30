package com.ddiehl.android.htn;

import com.ddiehl.android.htn.events.responses.UserIdentityRetrievedEvent;
import com.ddiehl.reddit.identity.UserIdentity;
import com.squareup.otto.Subscribe;

public interface IdentityManager {
    UserIdentity getUserIdentity();

    @Subscribe
    void onUserIdentityRetrieved(UserIdentityRetrievedEvent event);

    void saveUserIdentity(UserIdentity identity);

    void clearSavedUserIdentity();
}
