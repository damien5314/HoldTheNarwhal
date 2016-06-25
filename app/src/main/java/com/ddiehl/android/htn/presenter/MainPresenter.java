package com.ddiehl.android.htn.presenter;

import android.net.Uri;
import android.support.annotation.NonNull;

import rx.functions.Action1;
import rxreddit.model.AccessToken;

public interface MainPresenter extends BasePresenter {

  String getAuthorizationUrl();
  void onSignIn(String callbackUrl);
  void signOutUser();
  void onAnalyticsAccepted();
  void onAnalyticsDeclined();
  boolean customTabsEnabled();
  Action1<AccessToken> getUserIdentity();
  void onNavigateToSubreddit();
  void onLogIn();
  void onShowInbox();
  void onShowUserProfile();
  void onShowSubreddits();
  void onShowFrontPage();
  void onShowAllListings();
  void onShowRandomSubreddit();
  void processDeepLink(@NonNull Uri data);

}
