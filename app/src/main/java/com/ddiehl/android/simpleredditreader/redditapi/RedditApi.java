package com.ddiehl.android.simpleredditreader.redditapi;

import com.ddiehl.android.simpleredditreader.redditapi.comments.CommentsResponse;
import com.ddiehl.android.simpleredditreader.redditapi.listings.ListingsResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface RedditApi {

    @GET("/r/{subreddit}/hot.json")
    void getHotListings(@Path("subreddit") String subreddit,
                        Callback<ListingsResponse> callback);

    @GET("/r/{subreddit}/new.json")
    void getNewListings(@Path("subreddit") String subreddit,
                        Callback<ListingsResponse> callback);

    @GET("/hot.json")
    void getDefaultHotListings(Callback<ListingsResponse> callback);

    @GET("/r/{subreddit}/comments/{articleId}/hot.json")
    void getHotComments(@Path("subreddit") String subreddit,
                        @Path("articleId") String articleId,
                        Callback<List<CommentsResponse>> callback);

}