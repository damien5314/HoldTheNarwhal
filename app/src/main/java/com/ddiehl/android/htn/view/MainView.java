/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view;

public interface MainView extends BaseView {

    void updateUserIdentity();

    void closeNavigationDrawer();
    void showLoginView();
    void showUserProfile();
    void showUserProfile(String show, String username);
    void showUserSubreddits();
    void showSubreddit(String subreddit);
    void showWebViewForURL(String url);

    void onUserAuthCodeReceived(String authCode);

}
