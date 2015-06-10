package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainPresenter {

    String getUsername();
    void setUsernameContext(String username);
    UserIdentity getAuthorizedUser();
    void signOutUser();

}
