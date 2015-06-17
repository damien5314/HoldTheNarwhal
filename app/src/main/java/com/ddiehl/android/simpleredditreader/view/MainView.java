package com.ddiehl.android.simpleredditreader.view;

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
