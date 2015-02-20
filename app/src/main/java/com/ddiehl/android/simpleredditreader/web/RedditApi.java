package com.ddiehl.android.simpleredditreader.web;

import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RedditApi {

    @GET("/r/{subreddit}/{sort}.json")
    void getLinks(@Path("subreddit") String subreddit,
                  @Path("sort") String sort,
                  @Query("t") String timespan,
                  @Query("after") String after,
                  Callback<ListingResponse<RedditLink>> callback);

    @GET("/{sort}.json")
    void getDefaultLinks(@Path("sort") String sort,
                         @Query("t") String timespan,
                         @Query("after") String after,
                         Callback<ListingResponse<RedditLink>> callback);

    @GET("/r/{subreddit}/comments/{articleId}/hot.json")
    void getComments(@Path("subreddit") String subreddit,
                     @Path("articleId") String articleId,
                     Callback<List<ListingResponse<Listing>>> callback);

//    @GET("/r/{subreddit}/comments/{articleId}/hot.json")
//    void getComments(@Path("subreddit") String subreddit,
//                        @Path("articleId") String articleId,
//                        Callback<Response> callback);

    @POST("/api/vote")
    void vote(@Query("id") String id,
              @Query("dir") int dir,
              Callback<Response> response);

}