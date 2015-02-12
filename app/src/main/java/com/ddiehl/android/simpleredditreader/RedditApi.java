package com.ddiehl.android.simpleredditreader;

import com.ddiehl.android.simpleredditreader.redditapi.listings.RedditResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface RedditApi {

    @GET("/r/{subreddit}/hot.json") // Synchronous
    RedditResponse getHotListing(@Path("subreddit") String subreddit);

    @GET("/r/{subreddit}/hot.json")
    void getHotListing(@Path("subreddit") String subreddit, Callback<List<Link>> callback);

}