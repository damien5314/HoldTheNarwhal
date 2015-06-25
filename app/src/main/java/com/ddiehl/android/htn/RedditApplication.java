package com.ddiehl.android.htn;

import android.app.Application;

import com.ddiehl.android.htn.io.RedditService;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;


public class RedditApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Bus bus = BusProvider.getInstance();
        bus.register(this); // Listen for global events

        RedditPreferences prefs = RedditPreferences.getInstance(this);
        bus.register(prefs);

        RedditService authProxy = RedditServiceAuth.getInstance(this);
        bus.register(authProxy);

        if (BuildConfig.DEBUG)
            Picasso.with(this).setIndicatorsEnabled(true);

        HTNAnalytics analytics = HTNAnalytics.getInstance();
        analytics.init(this);
        bus.register(analytics);
    }

}
