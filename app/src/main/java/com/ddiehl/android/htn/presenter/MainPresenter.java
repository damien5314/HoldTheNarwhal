/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.presenter;

public interface MainPresenter {

    void onApplicationStart();
    void onApplicationStop();
    void signOutUser();
    String getUsernameContext();
    void setUsernameContext(String username);
    void onAnalyticsAccepted();
    void onAnalyticsDeclined();
}
