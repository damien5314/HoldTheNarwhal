package com.ddiehl.android.simpleredditreader.web;

import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface RedditApi {

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getApplicationAuthToken(@Field("grant_type") String grantType,
                                 @Field("device_id") String deviceId,
                                 Callback<AccessTokenResponse> callback);

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getUserAccessToken(@Field("grant_type") String grantType,
                            @Field("code") String code,
                            @Field("redirect_uri") String redirectUri,
                            Callback<AccessTokenResponse> callback);

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

    @POST("/api/vote")
    void vote(@Query("id") String id,
              @Query("dir") int dir,
              Callback<Response> response);

}