package com.ddiehl.android.htn.presenter;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainPresenter {

    UserIdentity getAuthorizedUser();
    void signOutUser();
    String getUsernameContext();
    void setUsernameContext(String username);
}
