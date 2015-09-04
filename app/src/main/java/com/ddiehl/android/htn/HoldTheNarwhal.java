/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.app.Application;

import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.otto.Bus;


public class HoldTheNarwhal extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);

        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());


        // Initialize static dependencies
        Analytics analytics = Analytics.getInstance();
        AccessTokenManager accessTokenManager = AccessTokenManager.getInstance(this);
        IdentityManager identityManager = IdentityManager.getInstance(this);
        SettingsManager settingsManager = SettingsManager.getInstance(this);
        RedditService authProxy = RedditServiceAuth.getInstance(this);

        Bus bus = BusProvider.getInstance();
        bus.register(analytics);
        bus.register(accessTokenManager);
        bus.register(identityManager);
        bus.register(settingsManager);
        bus.register(authProxy);
    }
}
