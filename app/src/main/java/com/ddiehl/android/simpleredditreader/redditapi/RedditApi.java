package com.ddiehl.android.simpleredditreader.redditapi;

import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingResponse;

import retrofit.http.GET;
import retrofit.http.Path;

public interface RedditApi {

    @GET("/r/{subreddit}/hot.json")
    ListingResponse getHotListing(@Path("subreddit") String subreddit);

}