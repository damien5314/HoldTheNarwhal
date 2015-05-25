package com.ddiehl.android.simpleredditreader;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.exceptions.ApiErrorEvent;
import com.ddiehl.android.simpleredditreader.io.RedditService;
import com.ddiehl.android.simpleredditreader.io.RedditServiceAuth;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;


public class RedditReaderApplication extends Application {
    private static final String TAG = RedditReaderApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating RedditReaderApplication");

        Bus bus = BusProvider.getInstance();
        bus.register(this); // Listen for global events

        RedditPreferences prefs = RedditPreferences.getInstance(this);
        bus.register(prefs);

        RedditService authProxy = RedditServiceAuth.getInstance(this);
        bus.register(authProxy);

//        UserIdentityInteractor userIdentityInteractor = new UserIdentityInteractor(this);
//        bus.register(userIdentityInteractor);

        // Stetho debugging tool
//        Stetho.initialize(Stetho.newInitializerBuilder(this)
//                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
//                .build());
//
//        // Network inspection through Stetho
//        new OkHttpClient().networkInterceptors().add(new StethoInterceptor());

        if (BuildConfig.DEBUG)
            Picasso.with(this).setIndicatorsEnabled(true);
    }

    @Subscribe
    public void onApiError(ApiErrorEvent event) {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getErrorMessage());
    }
}
