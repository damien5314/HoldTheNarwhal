package com.ddiehl.android.simpleredditreader;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.events.ApiErrorEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.redditapi.RedditApi;
import com.ddiehl.android.simpleredditreader.redditapi.RedditService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.RestAdapter;

/**
 * Created by Damien on 1/19/2015.
 */
public class RedditReaderApplication extends Application {
    private static final String TAG = RedditReaderApplication.class.getSimpleName();
    private final String API_URL = "http://www.reddit.com";

    private RedditService mRedditService;
    private Bus mBus = BusProvider.getInstance();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating RedditReaderApplication");

        mRedditService = new RedditService(buildApi(), mBus);
        mBus.register(mRedditService);

        mBus.register(this); // Listen for "global" events
    }

    private RedditApi buildApi() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .build();

        return restAdapter.create(RedditApi.class);
    }

    @Subscribe
    public void onApiError(ApiErrorEvent event) {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        Log.e("ReaderApp", event.getErrorMessage());
    }
}
