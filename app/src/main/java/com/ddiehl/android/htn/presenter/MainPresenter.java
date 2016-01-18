package com.ddiehl.android.htn.presenter;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ddiehl.reddit.identity.AccessToken;

import rx.functions.Action1;

public interface MainPresenter extends BasePresenter {
  void signOutUser();
  void onAnalyticsAccepted();
  void onAnalyticsDeclined();
  boolean customTabsEnabled();
  void onAuthCodeReceived(String authCode);
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
