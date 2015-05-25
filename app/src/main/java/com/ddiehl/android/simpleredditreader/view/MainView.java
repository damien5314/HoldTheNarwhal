package com.ddiehl.android.simpleredditreader.view;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainView {

    void showSpinner(String msg);
    void showSpinner(int resId);
    void dismissSpinner();
    void showToast(int resId);
    void showToast(String string);

    void closeNavigationDrawer();
    void setAccount(UserIdentity identity);
    void onUserSignOut();

    void showSubreddit(String subreddit);
    void openWebViewForURL(String authorizationUrl);
}
