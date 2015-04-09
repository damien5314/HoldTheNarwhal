package com.ddiehl.android.simpleredditreader;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.events.ApiErrorEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.web.IRedditService;
import com.ddiehl.android.simpleredditreader.web.RedditAuthProxy;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by Damien on 1/19/2015.
 */
public class RedditReaderApplication extends Application {
    private static final String TAG = RedditReaderApplication.class.getSimpleName();

    public static final String USER_AGENT =
            "android:com.ddiehl.android.simpleredditreader:v0.1 (by /u/damien5314)";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating RedditReaderApplication");

        Bus mBus = BusProvider.getInstance();
        mBus.register(this); // Listen for "global" events

        // Stetho debugging tool
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());

        // Network inspection through Stetho
        new OkHttpClient().networkInterceptors().add(new StethoInterceptor());
    }

    @Subscribe
    public void onApiError(ApiErrorEvent event) {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        Log.e(TAG, event.getErrorMessage());
    }
}
