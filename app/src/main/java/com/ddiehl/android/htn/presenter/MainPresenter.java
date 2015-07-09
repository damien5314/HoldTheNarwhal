/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

import com.ddiehl.android.htn.events.AppInitializedEvent;
import com.ddiehl.reddit.identity.UserIdentity;

public interface MainPresenter {

    UserIdentity getAuthorizedUser();
    void signOutUser();
    String getUsernameContext();
    void setUsernameContext(String username);
    void onAppInitialized(AppInitializedEvent event);
}
