package com.ddiehl.android.simpleredditreader.presenter;

public interface MainPresenter {

    void presentLoginView();
    void showSubreddit(String subreddit);
    void showUserProfile(String userId);
    void showSubreddits();

}
