package com.ddiehl.android.htn.io;

import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.AddCommentResponse;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.Subreddit;
import com.ddiehl.reddit.listings.TrophyResponse;
import com.ddiehl.reddit.listings.UserIdentityListing;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.util.List;

import retrofit.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface RedditAPI {
  @GET("/api/info")
  Observable<Response<ListingResponse>> getInfo(
      @Query("id") String id);

  @GET("/r/{subreddit}/api/info")
  Observable<Response<ListingResponse>> getInfo(
      @Path("subreddit") String subreddit,
      @Query("id") String id);

  @GET("/r/{subreddit}/about")
  Observable<Response<Subreddit>> getSubredditInfo(
      @Path("subreddit") String subreddit);

  @GET("/api/v1/me")
  Observable<Response<UserIdentity>> getUserIdentity();

  @GET("/api/v1/me/prefs")
  Observable<Response<UserSettings>> getUserSettings();

  @PATCH("/api/v1/me/prefs")
  Observable<Response<ResponseBody>> updateUserSettings(@Body RequestBody json);

  @GET("/{sort}.json")
  Observable<Response<ListingResponse>> getLinks(
      @Path("sort") String sort,
      @Query("r") String subreddit,
      @Query("t") String timespan,
      @Query("after") String after);

  @GET("/r/{subreddit}/comments/{articleId}.json")
  Observable<Response<List<ListingResponse>>> getComments(
      @Path("subreddit") String subreddit,
      @Path("articleId") String articleId,
      @Query("sort") String sort,
      @Query("comment") String commentId);

  @GET("/api/morechildren?api_type=json")
  Observable<Response<MoreChildrenResponse>> getMoreChildren(
      @Query("link_id") String linkId,
      @Query("children") String children,
      @Query("sort") String sort);

  @GET("/user/{username}/{show}")
  Observable<Response<ListingResponse>> getUserProfile(
      @Path("show") String show,
      @Path("username") String username,
      @Query("sort") String sort,
      @Query("t") String timespan,
      @Query("after") String after);

  @GET("/user/{username}/about")
  Observable<Response<UserIdentityListing>> getUserInfo(
      @Path("username") String username);

  @GET("/api/v1/me/friends/{username}")
  Observable<Response<FriendInfo>> getFriendInfo(
      @Path("username") String username);

  @GET("/api/v1/user/{username}/trophies")
  Observable<Response<TrophyResponse>> getUserTrophies(
      @Path("username") String username);

  @POST("/api/vote")
  Observable<Response<ResponseBody>> vote(
      @Query("id") String id,
      @Query("dir") int dir);

  @POST("/api/save")
  Observable<Response<ResponseBody>> save(
      @Query("id") String id,
      @Query("category") String category);

  @POST("/api/unsave")
  Observable<Response<ResponseBody>> unsave(
      @Query("id") String id);

  @POST("/api/hide")
  Observable<Response<ResponseBody>> hide(
      @Query("id") String id);

  @POST("/api/unhide")
  Observable<Response<ResponseBody>> unhide(
      @Query("id") String id);

  @POST("/api/report?api_type=json")
  Observable<Response<ResponseBody>> report(
      @Query("thing_id") String id,
      @Query("reason") String reason,
      @Query("otherReason") String otherReason);

  @POST("/api/comment?api_type=json")
  Observable<Response<AddCommentResponse>> addComment(
      @Query("parent") String parentId,
      @Query("text") String commentText);

  @PUT("/api/v1/me/friends/{username}")
  Observable<Response<ResponseBody>> addFriend(
      @Path("username") String username,
      @Body RequestBody json);

  @DELETE("/api/v1/me/friends/{username}")
  Observable<Response<ResponseBody>> deleteFriend(
      @Path("username") String username);
}