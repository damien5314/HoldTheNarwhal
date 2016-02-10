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

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
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
      @Query("before") String before,
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

  @GET("/message/{show}")
  Observable<ListingResponse> getInbox(
      @Path("show") String show,
      @Query("after") String after);

  @POST("/api/read_all_messages")
  Observable<Void> markAllMessagesRead();

  @POST("/api/read_message")
  Observable<Void> markMessagesRead(
      @Query("id") String commaSeparatedFullnames);

  @POST("/api/unread_message")
  Observable<Void> markMessagesUnread(
      @Query("id") String commaSeparatedFullnames);
}