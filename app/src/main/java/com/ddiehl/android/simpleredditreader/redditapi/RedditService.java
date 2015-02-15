package com.ddiehl.android.simpleredditreader.redditapi;

import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.ListingsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotListingsEvent;
import com.ddiehl.android.simpleredditreader.redditapi.comments.CommentsResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingsResponse;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

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
            mApi.getDefaultHotListings(new Callback<ListingsResponse>() {
                @Override
                public void success(ListingsResponse listingsResponse, Response response) {
                    mBus.post(new ListingsLoadedEvent(listingsResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage());
                }
            });
        } else {
            mApi.getHotListings(subreddit, new Callback<ListingsResponse>() {
                @Override
                public void success(ListingsResponse listingsResponse, Response response) {
                    mBus.post(new ListingsLoadedEvent(listingsResponse));
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
        if (subreddit == null || article == null) {
            Log.e(TAG, "Null parameter passed as LoadHotComments event");
            return;
        }

        mApi.getHotComments(subreddit, article, new Callback<List<CommentsResponse>>() {
            @Override
            public void success(List<CommentsResponse> commentsResponse, Response response) {
                mBus.post(new CommentsLoadedEvent(commentsResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage());
            }
        });
    }
}
