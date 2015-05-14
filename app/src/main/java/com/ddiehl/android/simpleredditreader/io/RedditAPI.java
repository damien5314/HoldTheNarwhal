package com.ddiehl.android.simpleredditreader.io;

import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;

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

    @GET("/r/{subreddit}/comments/{articleId}.json")
    void getComments(@Path("subreddit") String subreddit,
                     @Path("articleId") String articleId,
                     @Query("sort") String sort,
                     Callback<List<ListingResponse>> callback);

    @GET("/r/{subreddit}/comments/{articleId}.json")
    void getCommentThread(@Path("subreddit") String subreddit,
                     @Path("articleId") String articleId,
                     @Query("comment") String commentId,
                     @Query("sort") String sort,
                     @Query("context") Integer context,
                     Callback<List<ListingResponse>> callback);

    /** https://snap.apigee.com/1cqZR33 */
    @POST("/api/morechildren?api_type=json")
    void getMoreChildren(@Query("link_id") String linkId,
                         @Query("children") String children,
                         @Query("sort") String sort,
                         Callback<MoreChildrenResponse> callback);

    @POST("/api/vote")
    void vote(@Query("id") String id,
              @Query("dir") int dir,
              Callback<Response> response);

    @POST("/api/save")
    void save(@Query("id") String id,
              @Query("category") String category,
              Callback<Response> response);

    @POST("/api/unsave")
    void unsave(@Query("id") String id,
                Callback<Response> response);

    @POST("/api/report?api_type=json")
    void report(@Query("thing_id") String id,
                @Query("reason") String reason,
                @Query("otherReason") String otherReason,
                Callback<Response> response);

}