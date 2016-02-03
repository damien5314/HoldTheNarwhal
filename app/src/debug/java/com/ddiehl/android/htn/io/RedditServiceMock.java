package com.ddiehl.android.htn.io;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.CommentDeserializer;
import com.ddiehl.reddit.adapters.ListingDeserializer;
import com.ddiehl.reddit.adapters.ListingResponseDeserializer;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.AddCommentResponse;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.Subreddit;
import com.ddiehl.reddit.listings.TrophyResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RedditServiceMock implements RedditService {
  private Gson mGson;

  private RedditServiceMock() {
    mGson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
        .registerTypeAdapter(Listing.class, new ListingDeserializer())
        .registerTypeAdapter(AbsComment.class, new CommentDeserializer())
        .create();
  }

  @Override
  public Observable<UserIdentity> getUserIdentity() {
    UserIdentity response = mGson.fromJson(
        getReaderForFile("user_identity.json"), UserIdentity.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<UserSettings> getUserSettings() {
    UserSettings response = mGson.fromJson(
        getReaderForFile("user_settings.json"), UserSettings.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<ResponseBody> updateUserSettings(Map<String, String> settings) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ListingResponse> loadLinks(
      @Nullable String subreddit, @Nullable String sort,
      @Nullable String timespan, @Nullable String after) {
    ListingResponse response = mGson.fromJson(
        getReaderForFile("all_subreddits.json"), ListingResponse.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<List<ListingResponse>> loadLinkComments(
      @NonNull String subreddit, @NonNull String article,
      @Nullable String sort, @Nullable String commentId) {
    Type listType = new TypeToken<List<ListingResponse>>(){}.getType();
    List<ListingResponse> response = mGson.fromJson(
        getReaderForFile("link_comments.json"), listType);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<MoreChildrenResponse> loadMoreChildren(
      @NonNull Link link, @NonNull CommentStub moreComments,
      @NonNull List<String> children, @Nullable String sort) {
    MoreChildrenResponse response = mGson.fromJson(
        getReaderForFile("more_comments.json"), MoreChildrenResponse.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<UserIdentity> getUserInfo(@NonNull String username) {
    UserIdentity response = mGson.fromJson(
        getReaderForFile("user_identity.json"), UserIdentity.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<FriendInfo> getFriendInfo(String username) {
    FriendInfo response = mGson.fromJson(
        getReaderForFile("user_identity_friend_info.json"), FriendInfo.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<List<Listing>> getUserTrophies(@NonNull String username) {
    TrophyResponse response = mGson.fromJson(
        getReaderForFile("user_identity_friend_trophies.json"), TrophyResponse.class);
    return delay(Observable.just(response)
        .map(response2 -> response2.getData().getTrophies()));
  }

  @Override
  public Observable<ListingResponse> loadUserProfile(
      @NonNull String show, @NonNull String username,
      @Nullable String sort, @Nullable String timespan, @Nullable String after) {
    ListingResponse response = mGson.fromJson(
        getReaderForFile("user_profile_" + show + ".json"), ListingResponse.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<ResponseBody> addFriend(@NonNull String username) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ResponseBody> deleteFriend(@NonNull String username) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ResponseBody> saveFriendNote(@NonNull String username, @NonNull String note) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<Subreddit> getSubredditInfo(@NonNull String subreddit) {
    Subreddit response = mGson.fromJson(
        getReaderForFile("subreddit_info.json"), Subreddit.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<ResponseBody> vote(@NonNull Votable votable, int direction) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ResponseBody> save(
      @NonNull Savable listing, @Nullable String category, boolean save) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ResponseBody> hide(@NonNull Hideable listing, boolean toHide) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<ResponseBody> report(@NonNull String id, @NonNull String reason) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<Comment> addComment(@NonNull String parentId, @NonNull String text) {
    AddCommentResponse response = mGson.fromJson(
        getReaderForFile("add_comment.json"), AddCommentResponse.class);
    return delay(Observable.just(response)
        .map(AddCommentResponse::getComment));
  }

  @Override
  public Observable<ListingResponse> getInbox(@NonNull String show, @Nullable String after) {
    ListingResponse response = mGson.fromJson(
        getReaderForFile("inbox_" + show + ".json"), ListingResponse.class);
    return delay(Observable.just(response));
  }

  @Override
  public Observable<Void> markAllMessagesRead() {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<Void> markMessagesRead(@NonNull String commaSeparatedFullnames) {
    return delay(Observable.just(null));
  }

  @Override
  public Observable<Void> markMessagesUnread(@NonNull String commaSeparatedFullnames) {
    return delay(Observable.just(null));
  }

  private static Reader getReaderForFile(String filename) {
    InputStream is;
    try {
      is = HoldTheNarwhal.getContext().getAssets().open(filename);
      return new InputStreamReader(is);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static <T> Observable<T> delay(Observable<T> observable) {
    return observable
        .delay(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread());
  }

  ///////////////
  // Singleton //
  ///////////////

  private static RedditServiceMock _instance;

  public static RedditServiceMock getInstance() {
    if (_instance == null) {
      synchronized (RedditServiceMock.class) {
        if (_instance == null) {
          _instance = new RedditServiceMock();
        }
      }
    }
    return _instance;
  }
}
