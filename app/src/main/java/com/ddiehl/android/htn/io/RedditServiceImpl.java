package com.ddiehl.android.htn.io;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ddiehl.android.dlogger.Logger;
import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.io.interceptors.AuthorizationInterceptor;
import com.ddiehl.android.htn.io.interceptors.LoggingInterceptor;
import com.ddiehl.android.htn.io.interceptors.RawResponseInterceptor;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.AbsCommentDeserializer;
import com.ddiehl.reddit.adapters.ListingDeserializer;
import com.ddiehl.reddit.adapters.ListingResponseDeserializer;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.Friend;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.Comment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.ddiehl.reddit.listings.Subreddit;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.util.List;
import java.util.Map;

import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RedditServiceImpl implements RedditService {
  private Logger mLogger = HoldTheNarwhal.getLogger();
  private AccessTokenManager mAccessTokenManager = HoldTheNarwhal.getAccessTokenManager();
  private Analytics mAnalytics = HoldTheNarwhal.getAnalytics();
  private RedditAPI mAPI = buildApi();

  private RedditAPI buildApi() {
    final int cacheSize = 10 * 1024 * 1024; // 10 MiB
    OkHttpClient client = new OkHttpClient();
    Context context = HoldTheNarwhal.getContext();
    File cache = new File(context.getCacheDir().getAbsolutePath(), "htn-http-cache");
    client.setCache(new Cache(cache, cacheSize));
    client.networkInterceptors().add(new RawResponseInterceptor());
    client.networkInterceptors().add(
        AuthorizationInterceptor.get(AuthorizationInterceptor.Type.TOKEN_AUTH));
    client.networkInterceptors().add(new LoggingInterceptor());
    client.networkInterceptors().add(new StethoInterceptor());

    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
        .registerTypeAdapter(Listing.class, new ListingDeserializer())
        .registerTypeAdapter(AbsComment.class, new AbsCommentDeserializer())
        .create();

    Retrofit restAdapter = new Retrofit.Builder()
        .client(client)
        .baseUrl(ENDPOINT_OAUTH)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .build();

    return restAdapter.create(RedditAPI.class);
  }

  @Override
  public Observable<UserIdentity> getUserIdentity() {
    return requireUserAccessToken().flatMap(token ->
        mAPI.getUserIdentity()
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(response -> mAnalytics.logSignIn(response.body()))
            .map(Response::body));
  }

  @Override
  public Observable<UserSettings> getUserSettings() {
    return requireUserAccessToken().flatMap(token ->
        mAPI.getUserSettings()
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> updateUserSettings(Map<String, String> settings) {
    String json = new Gson().toJson(settings);
    RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
    return requireUserAccessToken().flatMap(token ->
        mAPI.updateUserSettings(body)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ListingResponse> loadLinks(
      @Nullable String subreddit, @Nullable String sort,
      @Nullable String timespan, @Nullable String after) {
    return requireAccessToken().flatMap(token ->
        mAPI.getLinks(sort, subreddit, timespan, after)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<List<ListingResponse>> loadLinkComments(
      @NonNull String subreddit, @NonNull String article,
      @Nullable String sort, @Nullable String commentId) {
    return requireAccessToken().flatMap(token ->
        mAPI.getComments(subreddit, article, sort, commentId)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<MoreChildrenResponse> loadMoreChildren(
      @NonNull Link link, @NonNull CommentStub parentStub,
      @NonNull List<String> children, @Nullable String sort) {
    StringBuilder b = new StringBuilder();
    for (String child : children) b.append(child).append(",");
    String childrenString = b.substring(0, Math.max(b.length() - 1, 0));
    return requireAccessToken().flatMap(token ->
        mAPI.getMoreChildren(link.getName(), childrenString, sort)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<UserIdentity> getUserInfo(@NonNull String username) {
    return requireAccessToken().flatMap(token ->
        mAPI.getUserInfo(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(response -> response.body().getUser()));
  }

  @Override
  public Observable<FriendInfo> getFriendInfo(String username) {
    return requireUserAccessToken().flatMap(token ->
        mAPI.getFriendInfo(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<List<Listing>> getUserTrophies(@NonNull String username) {
    return requireAccessToken().flatMap(token ->
        mAPI.getUserTrophies(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(response -> response.body().getData().getTrophies()));
  }

  @Override
  public Observable<ListingResponse> loadUserProfile(
      @NonNull String show, @NonNull String username, @Nullable String sort,
      @Nullable String timespan, @Nullable String after) {
    return requireAccessToken().flatMap(token ->
        mAPI.getUserProfile(show, username, sort, timespan, after)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> addFriend(@NonNull String username) {
    String json = "{}";
    RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
    return requireUserAccessToken().flatMap(token ->
        mAPI.addFriend(username, body)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> deleteFriend(@NonNull String username) {
    return requireUserAccessToken().flatMap(token ->
        mAPI.deleteFriend(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> saveFriendNote(@NonNull String username, @NonNull String note) {
    if (TextUtils.isEmpty(note)) {
      return Observable.error(new RuntimeException("User note should be non-empty"));
    }
    String json = new Gson().toJson(new Friend(note));
    return requireUserAccessToken().flatMap(token ->
        mAPI.addFriend(username, RequestBody.create(MediaType.parse("application/json"), json))
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<Subreddit> getSubredditInfo(@NonNull String subreddit) {
    return requireAccessToken().flatMap(token ->
        mAPI.getSubredditInfo(subreddit)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> vote(@NonNull Votable listing, int direction) {
    String fullname = String.format("%s_%s", listing.getKind(), listing.getId());
    return requireUserAccessToken().flatMap(token ->
        mAPI.vote(fullname, direction)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(response -> {
              if (response.message().contains("USER_REQUIRED")) {
                return Observable.error(new RuntimeException("Sign in required"));
              } else return Observable.just(response);
            })
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> save(
      @NonNull Savable listing, @Nullable String category, boolean toSave) {
    if (toSave) { // Save
      return requireUserAccessToken().flatMap(token ->
          mAPI.save(listing.getName(), category)
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(response -> {
                if (response.message().contains("USER_REQUIRED")) {
                  return Observable.error(new RuntimeException("Sign in required"));
                } else return Observable.just(response);
              })
              .map(Response::body));
    } else { // Unsave
      return requireUserAccessToken().flatMap(token ->
          mAPI.unsave(listing.getName())
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(response -> {
                if (response.message().contains("USER_REQUIRED")) {
                  return Observable.error(new RuntimeException("Sign in required"));
                } else return Observable.just(response);
              })
              .map(Response::body));
    }
  }

  @Override
  public Observable<ResponseBody> hide(@NonNull Hideable listing, boolean toHide) {
    if (toHide) { // Hide
      return requireUserAccessToken().flatMap(token ->
          mAPI.hide(listing.getName())
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(response -> {
                if (response.message().contains("USER_REQUIRED")) {
                  return Observable.error(new RuntimeException("Sign in required"));
                } else return Observable.just(response);
              })
              .map(Response::body));
    } else { // Unhide
      return mAPI.unhide(listing.getName())
          .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .flatMap(response -> {
            if (response.message().contains("USER_REQUIRED")) {
              return Observable.error(new RuntimeException("Sign in required"));
            } else return Observable.just(response);
          })
          .map(Response::body);
    }
  }

  @Override
  public Observable<ResponseBody> report(@NonNull String id, @NonNull String reason) {
    return Observable.error(new RuntimeException("Not yet implemented"));
  }

  @Override
  public Observable<Comment> addComment(@NonNull String parentId, @NonNull String text) {
//    String fullname = String.format("%1$s_%2$s", listing.getKind(), listing.getId());
    return requireUserAccessToken().flatMap(token ->
        mAPI.addComment(parentId, text)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(response -> response.body().getComment())
    );
  }

  private Observable<AccessToken> requireAccessToken() {
    return mAccessTokenManager.getAccessToken()
        .doOnError(error -> mLogger.e("No access token available"));
  }

  private Observable<UserAccessToken> requireUserAccessToken() {
    return mAccessTokenManager.getUserAccessToken()
        .doOnError(error -> mLogger.e("No user access token available"));
  }

  ///////////////
  // Singleton //
  ///////////////

  private static RedditServiceImpl _instance;

  public static RedditServiceImpl getInstance() {
    if (_instance == null) {
      synchronized (RedditServiceImpl.class) {
        if (_instance == null) {
          _instance = new RedditServiceImpl();
        }
      }
    }
    return _instance;
  }
}
