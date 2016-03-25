package com.ddiehl.android.htn;

import android.app.Application;
import android.content.Context;

import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.squareup.picasso.Picasso;

import rxreddit.api.RedditService;
import timber.log.Timber;

public class HoldTheNarwhal extends Application {
  private static Context mContext;

  @Override
  public void onCreate() {
    super.onCreate();
//    LeakCanary.install(this);
    Timber.plant(new Timber.DebugTree());

    if (BuildConfig.DEBUG) {
      Picasso.setSingletonInstance(
          new Picasso.Builder(this)
//              .memoryCache(Cache.NONE)
//              .indicatorsEnabled(true)
              .loggingEnabled(false)
              .build());
    }

    mContext = this;

    // Initialize static dependencies
    IdentityManager identityManager = getIdentityManager();
    SettingsManager settingsManager = getSettingsManager();
    RedditService api = getRedditService();
    Analytics analytics = getAnalytics();
    analytics.initialize();
  }

  public static Context getContext() {
    return mContext;
  }

  /**
   * Provides an instance of {@link IdentityManager} with which to track logged in reddit user
   * identity.
   * @return Instance of {@link IdentityManager}
   */
  public static IdentityManager getIdentityManager() {
    return IdentityManagerImpl.getInstance();
  }

  /**
   * Provides an instance of {@link SettingsManager} with which to track user's reddit and app
   * settings.
   * @return Instance of {@link SettingsManager}
   */
  public static SettingsManager getSettingsManager() {
    return SettingsManagerImpl.getInstance();
  }

  /**
   * Provides an instance of {@link RedditService} with which to call the reddit API
   * @return Instance of {@link RedditService}
   */
  public static RedditService getRedditService() {
//    return RedditService.getInstance();
    return HTNRedditService.getInstance();
//    return RedditServiceMock.getInstance();
  }

  /**
   * Provides an instance of {@link Analytics} to which to log application events
   * @return Instance of {@link Analytics}
   */
  public static Analytics getAnalytics() {
    return FlurryAnalytics.getInstance();
  }
}
