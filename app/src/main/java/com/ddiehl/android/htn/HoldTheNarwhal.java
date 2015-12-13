package com.ddiehl.android.htn;

import android.app.Application;
import android.content.Context;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.dlogger.NoLogger;
import com.ddiehl.android.dlogger.TimberLogger;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.android.htn.io.RedditAuthServiceImpl;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceImpl;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;


public class HoldTheNarwhal extends Application {
  private static Context mContext;

  @Override
  public void onCreate() {
    super.onCreate();
    LeakCanary.install(this);
    Logger logger = getLogger(); // Ensure the logger is loaded on application start

    Stetho.initialize(
        Stetho.newInitializerBuilder(this)
            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
            .build());

    mContext = this;

    // Initialize static dependencies
    AccessTokenManager accessTokenManager = getAccessTokenManager();
    IdentityManager identityManager = getIdentityManager();
    SettingsManager settingsManager = getSettingsManager();
    RedditService api = getRedditService();
    Analytics analytics = getAnalytics();
    analytics.initialize();
  }

  /**
   * Provides an instance of {@link Logger} with which to log debug messages.
   * @return Instance of {@link Logger}
   */
  public static Logger getLogger() {
    if (BuildConfig.DEBUG) return TimberLogger.getInstance();
    else return new NoLogger();
  }

  public static Context getContext() {
    return mContext;
  }

  /**
   * Provides an instance of {@link AccessTokenManager} with which to manage OAuth tokens.
   * @return Instance of {@link AccessTokenManager}
   */
  public static AccessTokenManager getAccessTokenManager() {
    return AccessTokenManagerImpl.getInstance();
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
   * Provides an instance of {@link RedditAuthServiceImpl} with which to retrieve OAuth2 tokens
   * @return Instance of {@link RedditAuthServiceImpl}
   */
  public static RedditAuthService getRedditServiceAuth() {
    return RedditAuthServiceImpl.getInstance();
  }

  /**
   * Provides an instance of {@link RedditService} with which to call the reddit API
   * @return Instance of {@link RedditService}
   */
  public static RedditService getRedditService() {
    return RedditServiceImpl.getInstance();
  }

  /**
   * Provides an instance of {@link Analytics} to which to log application events
   * @return Instance of {@link Analytics}
   */
  public static Analytics getAnalytics() {
    return FlurryAnalytics.getInstance();
  }
}
