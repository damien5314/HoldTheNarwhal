package com.ddiehl.android.simpleredditreader.redditapi;

import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotLinksEvent;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


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
    public void onLoadHotLinks(LoadHotLinksEvent event) {
        String subreddit = event.getSubreddit();
        if (subreddit == null) {
            mApi.getDefaultHotListings(new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage());
                }
            });
        } else {
            mApi.getHotListings(subreddit, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage());
                }
            });
        }
    }

    /**
     * Retrieves hot listings for article passed as a parameter
     */
    @Subscribe
    public void onLoadHotComments(LoadHotCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();

        mApi.getHotComments(subreddit, article, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                Log.d(TAG, "Number of ListingsResponse objects: " + listingsList.size());
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage());
            }
        });
    }
}
