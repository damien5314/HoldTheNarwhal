package com.ddiehl.android.simpleredditreader;

import android.app.Application;

import com.ddiehl.android.simpleredditreader.io.RedditService;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.flurry.android.FlurryAgent;
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

        String flurryApiKey = BuildConfig.DEBUG ?
                getString(R.string.flurry_api_key_debug) : getString(R.string.flurry_api_key);
        FlurryAgent.init(this, flurryApiKey);
        FlurryAgent.setContinueSessionMillis(30 * 1000); // Set Flurry session timeout to 30 seconds
    }
}
