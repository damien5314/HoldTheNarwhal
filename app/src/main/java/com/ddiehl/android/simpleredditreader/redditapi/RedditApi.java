package com.ddiehl.android.simpleredditreader.redditapi;

import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface RedditApi {

    @GET("/r/{subreddit}/hot.json")
    void getHotListings(@Path("subreddit") String subreddit, Callback<ListingResponse> callback);

    @GET("/r/{subreddit}/new.json")
    void getNewListings(@Path("subreddit") String subreddit, Callback<ListingResponse> callback);

    @GET("/hot.json")
    void getDefaultHotListings(Callback<ListingResponse> callback);

}