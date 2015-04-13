package com.ddiehl.android.simpleredditreader.model.auth;

import com.ddiehl.android.simpleredditreader.model.identity.UserIdentity;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RedditAPI {

    @GET("/api/v1/me")
    void getUserIdentity(Callback<UserIdentity> callback);

    @GET("/r/{subreddit}/{sort}.json")
    void getLinks(@Path("subreddit") String subreddit,
                  @Path("sort") String sort,
                  @Query("t") String timespan,
                  @Query("after") String after,
                  Callback<ListingResponse> callback);

    @GET("/{sort}.json")
    void getDefaultLinks(@Path("sort") String sort,
                         @Query("t") String timespan,
                         @Query("after") String after,
                         Callback<ListingResponse> callback);

    @GET("/r/{subreddit}/comments/{articleId}/.json")
    void getComments(@Path("subreddit") String subreddit,
                     @Path("articleId") String articleId,
                     @Query("sort") String sort,
                     Callback<List<ListingResponse>> callback);

    @GET("/api/morechildren")
    void getMoreChildren(@Query("link_id") String linkId,
                         @Query("sort") String sort,
                         @Query("children") String children,
                         @Query("depth") String depth,
                         @Query("id") String id,
//                         @Query("r") String subreddit,
                         Callback<List<ListingResponse>> callback);

    @POST("/api/vote")
    void vote(@Query("id") String id,
              @Query("dir") int dir,
              Callback<Response> response);

}