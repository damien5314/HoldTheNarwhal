package com.ddiehl.android.htn.presenter;

import android.support.annotation.Nullable;

import com.ddiehl.reddit.identity.AccessToken;

import rx.functions.Action1;

public interface MainPresenter extends BasePresenter {
  void signOutUser();
  void setUsernameContext(@Nullable String username);
  void onAnalyticsAccepted();
  void onAnalyticsDeclined();
  boolean customTabsEnabled();
  void onAuthCodeReceived(String authCode);
  Action1<AccessToken> getUserIdentity();
  void onNavigateToSubreddit();
  void onLogIn();
  void onShowUserProfile();
  void onShowSubreddits();
  void onShowFrontPage();
  void onShowAllListings();
  void onShowRandomSubreddit();
}
