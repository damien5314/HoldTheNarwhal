/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;


public class HoldTheNarwhal extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusProvider.getInstance();
        bus.register(this); // Listen for global events

        RedditPrefs prefs = RedditPrefs.getInstance(this);
        bus.register(prefs);

        RedditService authProxy = RedditServiceAuth.getInstance(this);
        bus.register(authProxy);

        if (BuildConfig.DEBUG)
            Picasso.with(this).setIndicatorsEnabled(true);

        HTNAnalytics analytics = HTNAnalytics.getInstance();
        analytics.init(this);
        bus.register(analytics);

        setMirroredIcons();
    }

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
