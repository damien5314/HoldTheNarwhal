package com.ddiehl.android.simpleredditreader.web;

import android.content.Context;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RedditService implements IRedditService {
    private static final String TAG = RedditService.class.getSimpleName();

    private Context mContext;
    private RedditApi mApi;
    private Bus mBus = BusProvider.getInstance();

    protected RedditService(Context context, RedditApi api) {
        mContext = context.getApplicationContext();
        mApi = api;
    }

    /**
     * Retrieves link listings for subreddit
     */
    public void onLoadLinks(LoadLinksEvent event) {
        Log.d(TAG, "RedditService.onLoadLinks");
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();

        if (subreddit == null) {
            mApi.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    Log.d(TAG, "getDefaultLinks success");
                    Utils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "getDefaultLinks failure");
                    Utils.showError(mContext, error);
                    Utils.printResponse(error.getResponse());
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        } else {
            mApi.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    Log.d(TAG, "getLinks success");
                    Utils.printResponseStatus(response);
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d(TAG, "getLinks failure");
                    Utils.showError(mContext, error);
                    Utils.printResponse(error.getResponse());
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        }
    }

    /**
     * Retrieves comment listings for link passed as parameter
     */
    public void onLoadComments(LoadCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();
        String sort = event.getSort();

        mApi.getComments(subreddit, article, sort, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                Utils.printResponseStatus(response);
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.showError(mContext, error);
                Utils.printResponse(error.getResponse());
                mBus.post(new CommentsLoadedEvent(error));
            }
        });
    }

    /**
     * Submits a vote on a link or comment
     */
    public void onVote(VoteEvent event) {
        String id = event.getId();
        int direction = event.getDirection();
        mApi.vote(id, direction, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                Utils.printResponseStatus(response);
                Utils.printResponseBody(response);
                mBus.post(new VoteSubmittedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                Utils.showError(mContext, error);
                Utils.printResponse(error.getResponse());
                mBus.post(new VoteSubmittedEvent(error));
            }
        });
    }

}
