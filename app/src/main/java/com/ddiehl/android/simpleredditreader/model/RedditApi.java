package com.ddiehl.android.simpleredditreader.model;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RedditApi {

    @GET("/r/{subreddit}/hot.json")
    void getHotListings(@Path("subreddit") String subreddit, @Query("after") String after,
                        Callback<ListingResponse<RedditLink>> callback);

    @GET("/hot.json")
    void getDefaultHotListings(Callback<ListingResponse<RedditLink>> callback);

//    @GET("/r/{subreddit}/comments/{articleId}/hot.json")
//    void getHotComments(@Path("subreddit") String subreddit,
//                        @Path("articleId") String articleId,
//                        Callback<List<ListingResponse<RedditComment>>> callback);

    @GET("/r/{subreddit}/comments/{articleId}/hot.json")
    void getHotComments(@Path("subreddit") String subreddit,
                        @Path("articleId") String articleId,
                        Callback<Response> callback);

}