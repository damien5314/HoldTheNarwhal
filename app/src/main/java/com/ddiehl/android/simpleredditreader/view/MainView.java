package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView extends BaseView {

    void setIdentity(UserIdentity identity);

    void closeNavigationDrawer();
    void showLoginView();
    void showUserProfile();
    void showUserProfile(String show, String username);
    void selectUserProfileTab(String show);
    void showUserSubreddits();
    void showSubreddit(String subreddit);
    void showWebViewForURL(String authorizationUrl);

    void onUserAuthCodeReceived(String authCode);

}
