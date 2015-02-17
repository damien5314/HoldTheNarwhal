package com.ddiehl.android.simpleredditreader.model;

import android.util.Log;

import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

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
    public void onLoadLinks(LoadLinksEvent event) {
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();
        Log.i(TAG, "Loading hot links for /r/" + subreddit + " after " + after);

        if (subreddit == null) {
            mApi.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse<RedditLink>>() {
                @Override
                public void success(ListingResponse<RedditLink> linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                }
            });
        } else {
            mApi.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse<RedditLink>>() {
                @Override
                public void success(ListingResponse<RedditLink> linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
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
        Log.i(TAG, "Loading hot links for /r/" + subreddit + "/" + article);

//        mApi.getHotComments(subreddit, article, new Callback<List<ListingResponse<RedditComment>>>() {
//            @Override
//            public void success(List<ListingResponse<RedditComment>> listingsList, Response response) {
//                mBus.post(new CommentsLoadedEvent(listingsList));
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//                Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
//            }
//        });

        mApi.getHotComments(subreddit, article, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                try {
                    JSONObject json = new JSONObject(Utils.inputStreamToString(response.getBody().in()));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
            }
        });
    }
}
