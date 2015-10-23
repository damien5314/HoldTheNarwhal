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

        // Initialize static dependencies
        Analytics analytics = HoldTheNarwhal.getAnalytics();
        bus.register(analytics);
        AccessTokenManager accessTokenManager = AccessTokenManager.getInstance(this);
        bus.register(accessTokenManager);
        IdentityManager identityManager = IdentityManager.getInstance(this);
        bus.register(identityManager);
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        bus.register(settingsManager);
        RedditService authProxy = RedditServiceAuth.getInstance(this);
        bus.register(authProxy);
    }

    /**
     * Provides an instance of Analytics to which to log application events
     * @return Instance of Analytics
     */
    public static Analytics getAnalytics() {
        return FlurryAnalytics.getInstance();
    }
}
