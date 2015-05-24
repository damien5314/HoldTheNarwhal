package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView {

    void setAccount(UserIdentity identity);
    void onUserSignOut();
}
