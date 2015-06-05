package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView {

    void setTitle(CharSequence title);
    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(String msg);
    void showToast(int resId);

    void closeNavigationDrawer();
    void setAccount(UserIdentity identity);
    void updateNavigationItems();

    void showLoginView();
    void showUserProfile(String userId);
    void showUserProfileOverview(String userId);
    void showUserProfileComments(String userId);
    void showUserSubreddits();
    void showSubreddit(String subreddit);
    void openWebViewForURL(String authorizationUrl);

    void onUserAuthCodeReceived(String authCode);

}
