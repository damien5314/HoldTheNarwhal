package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView extends BaseView {

    void closeNavigationDrawer();
    void setAccount(UserIdentity identity);
    void updateNavigationItems();

    void showLoginView();
    void showUserProfile();
    void showUserProfileOverview();
    void showUserProfileComments();
    void showUserSubreddits();
    void showSubreddit(String subreddit);
    void openWebViewForURL(String authorizationUrl);

    void onUserAuthCodeReceived(String authCode);

}
