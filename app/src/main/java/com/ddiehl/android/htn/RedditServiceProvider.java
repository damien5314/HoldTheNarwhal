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
    mRedditService = new RedditService.Builder()
        .appId(BuildConfig.REDDIT_APP_ID)
        .redirectUri(BuildConfig.REDDIT_REDIRECT_URI)
        .deviceId(AndroidUtil.getDeviceId(HoldTheNarwhal.getContext()))
        .userAgent(RxRedditUtil.getUserAgent(
            "android", "com.ddiehl.android.htn", BuildConfig.VERSION_NAME, "damien5314"))
        .accessTokenManager(new AndroidAccessTokenManager(HoldTheNarwhal.getContext()))
        .build();
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
