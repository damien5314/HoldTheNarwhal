/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.ddiehl.android.htn.events.AppInitializedEvent;
import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.mopub.common.MoPub;
import com.mopub.mobileads.MoPubConversionTracker;
import com.squareup.otto.Bus;


public class HoldTheNarwhal extends Application {

    public static boolean isInitialized = false;

    private final Bus mBus = BusProvider.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        init.execute();
    }

    private AsyncTask<Void, Void, Void> init = new AsyncTask<Void, Void, Void>() {
        AccessTokenManager atm;
        IdentityManager identityManager;
        SettingsManager settingsManager;
        RedditService authProxy;
        HTNAnalytics analytics;

        @Override
        protected Void doInBackground(Void... params) {
            atm = AccessTokenManager.getInstance(HoldTheNarwhal.this);
            identityManager = IdentityManager.getInstance(HoldTheNarwhal.this);
            settingsManager = SettingsManager.getInstance(HoldTheNarwhal.this);
            authProxy = RedditServiceAuth.getInstance(HoldTheNarwhal.this);
            analytics = HTNAnalytics.getInstance();
            analytics.init(HoldTheNarwhal.this);

            // MoPub configuration
            new MoPubConversionTracker().reportAppOpen(HoldTheNarwhal.this);
            MoPub.setLocationAwareness(MoPub.LocationAwareness.DISABLED);

            setMirroredIcons();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mBus.register(atm);
            mBus.register(identityManager);
            mBus.register(settingsManager);
            mBus.register(authProxy);
            mBus.register(analytics);

            isInitialized = true;
            mBus.post(new AppInitializedEvent());
        }
    };

    private void setMirroredIcons() {
        if (Build.VERSION.SDK_INT >= 19) {
            int[] ids = new int[] {
                    R.drawable.ic_action_refresh,
                    R.drawable.ic_sign_out,
                    R.drawable.ic_action_reply,
                    R.drawable.ic_action_save,
                    R.drawable.ic_action_share,
                    R.drawable.ic_action_show_comments,
                    R.drawable.ic_change_sort,
                    R.drawable.ic_change_timespan,
                    R.drawable.ic_navigation_go,
                    R.drawable.ic_saved,
                    R.drawable.ic_saved_dark
            };

            for (int id : ids) {
                Drawable res = ContextCompat.getDrawable(this, id);
                if (res != null) {
                    res.setAutoMirrored(true);
                }
            }
        }
    }
}
