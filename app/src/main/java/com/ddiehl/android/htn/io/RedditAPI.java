/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedString;

public interface RedditAPI {

    @GET("/api/v1/me")
    void getUserIdentity(Callback<UserIdentity> callback);

    @GET("/api/v1/me/prefs")
    void getUserSettings(Callback<UserSettings> callback);

    @PATCH("/api/v1/me/prefs")
    void updateUserSettings(@Body TypedString json, Callback<Response> callback);

    @GET("/{sort}.json")
    void getLinks(@Query("r") String subreddit,
                  @Path("sort") String sort,
                  @Query("t") String timespan,
                  @Query("after") String after,
                  Callback<ListingResponse> callback);

    @GET("/r/{subreddit}/comments/{articleId}.json")
    void getComments(@Path("subreddit") String subreddit,
                     @Path("articleId") String articleId,
                     @Query("sort") String sort,
                     @Query("comment") String commentId,
                     Callback<List<ListingResponse>> callback);

    /** https://snap.apigee.com/1cqZR33 */
    @GET("/api/morechildren?api_type=json")
    void getMoreChildren(@Query("link_id") String linkId,
                         @Query("children") String children,
                         @Query("sort") String sort,
                         Callback<MoreChildrenResponse> callback);

    @GET("/user/{username}/{show}")
    void getUserProfile(@Path("show") String show,
                        @Path("username") String username,
                        @Query("sort") String sort,
                        @Query("t") String timespan,
                        @Query("after") String after,
                        Callback<ListingResponse> callback);

    @POST("/api/vote")
    void vote(@Body String nullBody,
              @Query("id") String id,
              @Query("dir") int dir,
              Callback<Response> response);

    @POST("/api/save")
    void save(@Body String nullBody,
              @Query("id") String id,
              @Query("category") String category,
              Callback<Response> response);

    @POST("/api/unsave")
    void unsave(@Body String nullBody,
                @Query("id") String id,
                Callback<Response> response);

    @POST("/api/hide")
    void hide(@Body String nullBody,
              @Query("id") String id,
              Callback<Response> response);

    @POST("/api/unhide")
    void unhide(@Body String nullBody,
                @Query("id") String id,
                Callback<Response> response);

    @POST("/api/report?api_type=json")
    void report(@Body String nullBody,
                @Query("thing_id") String id,
                @Query("reason") String reason,
                @Query("otherReason") String otherReason,
                Callback<Response> response);

}