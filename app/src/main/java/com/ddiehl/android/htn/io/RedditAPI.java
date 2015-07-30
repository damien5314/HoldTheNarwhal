/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedString;
import rx.Observable;

public interface RedditAPI {

    @GET("/api/v1/me")
    Observable<UserIdentity> getUserIdentity();

    @GET("/api/v1/me/prefs")
    Observable<UserSettings> getUserSettings();

    @PATCH("/api/v1/me/prefs")
    Observable<Response> updateUserSettings(@Body TypedString json);

    @GET("/{sort}.json")
    Observable<ListingResponse> getLinks(
            @Query("r") String subreddit,
            @Path("sort") String sort,
            @Query("t") String timespan,
            @Query("after") String after);

    @GET("/r/{subreddit}/comments/{articleId}.json")
    Observable<List<ListingResponse>> getComments(
            @Path("subreddit") String subreddit,
            @Path("articleId") String articleId,
            @Query("sort") String sort,
            @Query("comment") String commentId);

    @GET("/api/morechildren?api_type=json")
    Observable<MoreChildrenResponse> getMoreChildren(
            @Query("link_id") String linkId,
            @Query("children") String children,
            @Query("sort") String sort);

    @GET("/user/{username}/{show}")
    Observable<ListingResponse> getUserProfile(
            @Path("show") String show,
            @Path("username") String username,
            @Query("sort") String sort,
            @Query("t") String timespan,
            @Query("after") String after);

    @POST("/api/vote")
    Observable<Response> vote(
            @Body String nullBody,
            @Query("id") String id,
            @Query("dir") int dir);

    @POST("/api/save")
    Observable<Response> save(
            @Body String nullBody,
            @Query("id") String id,
            @Query("category") String category);

    @POST("/api/unsave")
    Observable<Response> unsave(
            @Body String nullBody,
            @Query("id") String id);

    @POST("/api/hide")
    Observable<Response> hide(
            @Body String nullBody,
            @Query("id") String id);

    @POST("/api/unhide")
    Observable<Response> unhide(
            @Body String nullBody,
            @Query("id") String id);

    @POST("/api/report?api_type=json")
    Observable<Response> report(
            @Body String nullBody,
            @Query("thing_id") String id,
            @Query("reason") String reason,
            @Query("otherReason") String otherReason);

}