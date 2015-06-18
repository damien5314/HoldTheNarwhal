package com.ddiehl.android.simpleredditreader;

import android.app.Application;

import com.ddiehl.android.simpleredditreader.io.RedditService;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;


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

        HTNAnalytics analytics = HTNAnalytics.getInstance();
        analytics.init(this);
        bus.register(analytics);

        // Log if ads are enabled to Flurry
        Map<String, String> params = new HashMap<>();
        params.put("b", String.valueOf(prefs.getAdsEnabled()));
        FlurryAgent.logEvent("ads enabled", params);
    }
}
