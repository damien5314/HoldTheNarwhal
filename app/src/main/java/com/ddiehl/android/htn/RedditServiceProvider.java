package com.ddiehl.android.htn;

import rxreddit.RxRedditUtil;
import rxreddit.android.AndroidAccessTokenManager;
import rxreddit.android.AndroidUtil;
import rxreddit.api.RedditService;

public class RedditServiceProvider {

  private RedditService mRedditService;

  ///////////////
  // Singleton //
  ///////////////

  private static RedditServiceProvider _instance;

  private RedditServiceProvider() {
    mRedditService = new RedditService(
        BuildConfig.REDDIT_APP_ID,
        BuildConfig.REDDIT_REDIRECT_URI,
        AndroidUtil.getDeviceId(HoldTheNarwhal.getContext()),
        RxRedditUtil.getUserAgent(
            "android", "com.ddiehl.android.htn", BuildConfig.VERSION_NAME, "damien5314"),
        new AndroidAccessTokenManager(HoldTheNarwhal.getContext()));
  }

  public static RedditService getInstance() {
    if (_instance == null) {
      synchronized (RedditServiceProvider.class) {
        if (_instance == null) {
          _instance = new RedditServiceProvider();
        }
      }
    }
    return _instance.mRedditService;
  }
}
