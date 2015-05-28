package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.reddit.identity.UserIdentity;

public interface MainPresenter {

    void presentLoginView();
    void showUserProfile(String userId);
    void showUserSubreddits();
    void showSubreddit(String subreddit);

    UserIdentity getAuthorizedUser();
    void signOutUser();
}
