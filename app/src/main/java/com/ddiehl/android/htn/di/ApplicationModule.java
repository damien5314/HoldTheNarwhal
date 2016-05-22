package com.ddiehl.android.htn.di;

import android.content.Context;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.IdentityManagerImpl;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.SettingsManagerImpl;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.analytics.FlurryAnalytics;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rxreddit.RxRedditUtil;
import rxreddit.android.AndroidAccessTokenManager;
import rxreddit.android.AndroidUtil;
import rxreddit.api.RedditService;

@Module
public class ApplicationModule {

  private Context mContext;

  public ApplicationModule(Context context) {
    mContext = context.getApplicationContext();
  }

  @Provides
  Context providesContext() {
    return mContext;
  }

  @Singleton @Provides
  Analytics providesAnalytics() {
    return new FlurryAnalytics();
  }

  @Singleton @Provides
  IdentityManager providesIdentityManager(Context context, SettingsManager settingsManager) {
    return new IdentityManagerImpl(context, settingsManager);
  }

  @Singleton @Provides
  SettingsManager providesSettingsManager(Context context, Analytics analytics, RedditService service) {
    return new SettingsManagerImpl(context, analytics, service);
  }

  @Singleton @Provides
  RedditService providesRedditService(Context context) {
    return new RedditService.Builder()
        .appId(BuildConfig.REDDIT_APP_ID)
        .redirectUri(BuildConfig.REDDIT_REDIRECT_URI)
        .deviceId(AndroidUtil.getDeviceId(context))
        .userAgent(RxRedditUtil.getUserAgent(
            "android", "com.ddiehl.android.htn", BuildConfig.VERSION_NAME, "damien5314"))
        .accessTokenManager(new AndroidAccessTokenManager(context))
        .build();
  }
}
