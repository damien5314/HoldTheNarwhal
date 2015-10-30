package com.ddiehl.android.htn;

import android.app.Application;

import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.facebook.stetho.Stetho;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;


public class HoldTheNarwhal extends Application {
    public static final String TAG = "HTN";

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        Logger.init(TAG)
                .hideThreadInfo()
                .setMethodCount(0)
                .setLogLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());

        Bus bus = BusProvider.getInstance();
        AndroidContextProvider.setContext(this);

        // Initialize static dependencies
        Analytics analytics = getAnalytics();
        bus.register(analytics);
        AccessTokenManager accessTokenManager = getAccessTokenManager();
        bus.register(accessTokenManager);
        IdentityManager identityManager = getIdentityManager();
        bus.register(identityManager);
        SettingsManager settingsManager = getSettingsManager();
        bus.register(settingsManager);
        RedditService api = RedditServiceAuth.getInstance(this);
        bus.register(api);
    }

    /**
     * Provides an instance of AccessTokenManager with which to manage OAuth tokens
     * @return Instance of AccessTokenManager
     */
    public static AccessTokenManager getAccessTokenManager() {
        return AccessTokenManagerImpl.getInstance();
    }

    /**
     * Provides an instance of IdentityManager with which to track logged in reddit user identity
     * @return Instance of IdentityManager
     */
    public static IdentityManager getIdentityManager() {
        return IdentityManagerImpl.getInstance();
    }

    /**
     * Provides an instance of SettingsManager with which to track user's reddit and app settings
     * @return Instance of SettingsManager
     */
    public static SettingsManager getSettingsManager() {
        return SettingsManagerImpl.getInstance();
    }

    /**
     * Provides an instance of Analytics to which to log application events
     * @return Instance of Analytics
     */
    public static Analytics getAnalytics() {
        return FlurryAnalytics.getInstance();
    }
}
