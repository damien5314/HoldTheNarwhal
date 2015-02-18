package com.ddiehl.android.simpleredditreader;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.events.ApiErrorEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.web.RedditApi;
import com.ddiehl.android.simpleredditreader.web.RedditService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by Damien on 1/19/2015.
 */
public class RedditReaderApplication extends Application {
    private static final String TAG = RedditReaderApplication.class.getSimpleName();
    private final String API_URL = "http://www.reddit.com";
    private static final String USER_AGENT =
            "android:com.ddiehl.android.simpleredditreader:v0.1 (by /u/damien5314)";

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
//        Gson gson = new GsonBuilder()
//                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                .registerTypeAdapter(ListingResponse.class, new ListingResponseAdapter())
//                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL)
                // Add user agent header to each request
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", USER_AGENT);
                    }
                })
                .build();

        return restAdapter.create(RedditApi.class);
    }

    @Subscribe
    public void onApiError(ApiErrorEvent event) {
        Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
        Log.e("ReaderApp", event.getErrorMessage());
    }
}
