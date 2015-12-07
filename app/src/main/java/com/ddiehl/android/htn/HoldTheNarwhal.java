package com.ddiehl.android.htn;

import android.app.Application;

import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.analytics.FlurryAnalytics;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAPI;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.android.htn.logging.TimberLogger;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;


public class HoldTheNarwhal extends Application {
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

        Bus bus = BusProvider.getInstance();
        AndroidContextProvider.setContext(this);

        // Initialize static dependencies
        Analytics analytics = getAnalytics();
        analytics.initialize();
        bus.register(analytics);

        AccessTokenManager accessTokenManager = getAccessTokenManager();
        bus.register(accessTokenManager);

        IdentityManager identityManager = getIdentityManager();
        bus.register(identityManager);

        SettingsManager settingsManager = getSettingsManager();
        bus.register(settingsManager);

        RedditService api = getRedditService();
        bus.register(api);
    }

    /**
     * Provides an instance of {@link Logger} with which to log debug messages.
     * @return Instance of {@link Logger}
     */
    public static Logger getLogger() {
        return TimberLogger.getInstance();
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
     * Provides an instance of {@link RedditServiceAuth} with which to retrieve OAuth2 tokens
     * @return Instance of {@link RedditServiceAuth}
     */
    public static RedditServiceAuth getRedditServiceAuth() {
        return RedditServiceAuth.getInstance();
    }

    /**
     * Provides an instance of {@link RedditService} with which to call the reddit API
     * @return Instance of {@link RedditService}
     */
    public static RedditService getRedditService() {
        return RedditServiceAPI.getInstance();
    }

    /**
     * Provides an instance of {@link Analytics} to which to log application events
     * @return Instance of {@link Analytics}
     */
    public static Analytics getAnalytics() {
        return FlurryAnalytics.getInstance();
    }
}
