package com.ddiehl.android.simpleredditreader;

import android.app.Application;

import com.ddiehl.android.simpleredditreader.io.RedditService;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;


public class RedditApplication extends Application {
    private static final String TAG = RedditApplication.class.getSimpleName();

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
    }
}
