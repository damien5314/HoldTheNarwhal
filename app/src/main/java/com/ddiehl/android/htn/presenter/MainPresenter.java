package com.ddiehl.android.htn.presenter;

import android.support.annotation.Nullable;

public interface MainPresenter extends BasePresenter {
    void signOutUser();
    String getUsernameContext();
    void setUsernameContext(@Nullable String username);
    void onAnalyticsAccepted();
    void onAnalyticsDeclined();
    boolean customTabsEnabled();
    void onAuthCodeReceived(String authCode);
}
