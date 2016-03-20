package com.ddiehl.android.htn;

import rxreddit.api.RedditService;

public class HTNRedditService {
  public static final String USER_AGENT = String.format(
      "android:com.ddiehl.android.htn:v%s (by /u/damien5314)",
      BuildConfig.VERSION_NAME);

  private RedditService mRedditService;

  ///////////////
  // Singleton //
  ///////////////

  private static HTNRedditService _instance;

  private HTNRedditService() {
    mRedditService = new RedditService(
        BuildConfig.REDDIT_APP_ID,
        BuildConfig.REDDIT_REDIRECT_URI,
        HoldTheNarwhal.getSettingsManager().getDeviceId(),
        USER_AGENT,
        HoldTheNarwhal.getAccessTokenManager());
  }

  public static RedditService getInstance() {
    if (_instance == null) {
      synchronized (HTNRedditService.class) {
        if (_instance == null) {
          _instance = new HTNRedditService();
        }
      }
    }
    return _instance.mRedditService;
  }
}
