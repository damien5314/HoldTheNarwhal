package com.ddiehl.android.simpleredditreader.redditapi;

import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotListingsEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Damien on 1/19/2015.
 */
public class RedditService {
    private static final String TAG = RedditService.class.getSimpleName();

    private RedditApi mApi;
    private Bus mBus;

    public RedditService(RedditApi api, Bus bus) {
        mApi = api;
        mBus = bus;
    }

    /**
     * Retrieves /hot.json listings for subreddit passed as a parameter
     */
    @Subscribe
    public void onLoadHotListings(LoadHotListingsEvent event) {
        String subreddit = event.getSubreddit();
        if (subreddit == null) {
            mApi.getDefaultHotListings(new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse listingResponse, Response response) {
                    mBus.post(new ListingsLoadedEvent(listingResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage());
                }
            });
        } else {
            mApi.getHotListings(subreddit, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse listingResponse, Response response) {
                    mBus.post(new ListingsLoadedEvent(listingResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage());
                }
            });
        }
    }
}
