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
import com.ddiehl.android.htn.io.interceptors.UserAgentInterceptor;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.CommentDeserializer;
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
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.Cache;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
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
    Context context = HoldTheNarwhal.getContext();
    File cache = new File(context.getCacheDir().getAbsolutePath(), "htn-http-cache");
    OkHttpClient client = new OkHttpClient.Builder()
        .cache(new Cache(cache, cacheSize))
        .addNetworkInterceptor(new UserAgentInterceptor(RedditService.USER_AGENT))
        .addNetworkInterceptor(new RawResponseInterceptor())
        .addNetworkInterceptor(
            AuthorizationInterceptor.get(AuthorizationInterceptor.Type.TOKEN_AUTH))
        .addNetworkInterceptor(new LoggingInterceptor())
//        .addNetworkInterceptor(new StethoInterceptor())
        .build();

    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
        .registerTypeAdapter(Listing.class, new ListingDeserializer())
        .registerTypeAdapter(AbsComment.class, new CommentDeserializer())
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireUserAccessToken().flatMap(token ->
        mAPI.getUserIdentity()
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body)
            .doOnNext(response -> mAnalytics.logSignIn(response)));
  }

  @Override
  public Observable<UserSettings> getUserSettings() {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireUserAccessToken().flatMap(token ->
        mAPI.getUserSettings()
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> updateUserSettings(Map<String, String> settings) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.getComments(subreddit, article, sort, commentId)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<MoreChildrenResponse> loadMoreChildren(
      @NonNull Link link, @NonNull CommentStub parentStub,
      @NonNull List<String> childrenIds, @Nullable String sort) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    StringBuilder b = new StringBuilder();
    for (String child : childrenIds) b.append(child).append(",");
    String childrenString = b.substring(0, Math.max(b.length() - 1, 0));
    return requireAccessToken().flatMap(token ->
        mAPI.getMoreChildren(link.getFullName(), childrenString, sort)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<UserIdentity> getUserInfo(@NonNull String username) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.getUserInfo(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(response -> response.body().getUser()));
  }

  @Override
  public Observable<FriendInfo> getFriendInfo(String username) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireUserAccessToken().flatMap(token ->
        mAPI.getFriendInfo(username)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<List<Listing>> getUserTrophies(@NonNull String username) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.getUserProfile(show, username, sort, timespan, after)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> addFriend(@NonNull String username) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
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
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    String json = new Gson().toJson(new Friend(note));
    return requireUserAccessToken().flatMap(token ->
        mAPI.addFriend(username, RequestBody.create(MediaType.parse("application/json"), json))
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<Subreddit> getSubredditInfo(@NonNull String subreddit) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.getSubredditInfo(subreddit)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> vote(@NonNull Votable listing, int direction) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    String fullname = String.format("%s_%s", listing.getKind(), listing.getId());
    return requireUserAccessToken().flatMap(token ->
        mAPI.vote(fullname, direction)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap(this::checkForUserRequiredError)
            .map(Response::body));
  }

  @Override
  public Observable<ResponseBody> save(
      @NonNull Savable listing, @Nullable String category, boolean toSave) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    if (toSave) { // Save
      return requireUserAccessToken().flatMap(token ->
          mAPI.save(listing.getFullName(), category)
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(this::checkForUserRequiredError)
              .map(Response::body));
    } else { // Unsave
      return requireUserAccessToken().flatMap(token ->
          mAPI.unsave(listing.getFullName())
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(this::checkForUserRequiredError)
              .map(Response::body));
    }
  }

  @Override
  public Observable<ResponseBody> hide(@NonNull Hideable listing, boolean toHide) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    if (toHide) { // Hide
      return requireUserAccessToken().flatMap(token ->
          mAPI.hide(listing.getFullName())
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(this::checkForUserRequiredError)
              .map(Response::body));
    } else { // Unhide
      return requireUserAccessToken().flatMap(token ->
          mAPI.unhide(listing.getFullName())
              .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .flatMap(this::checkForUserRequiredError)
              .map(Response::body));
    }
  }

  @Override
  public Observable<ResponseBody> report(@NonNull String id, @NonNull String reason) {
    return Observable.error(new RuntimeException("Not yet implemented"));
  }

  @Override
  public Observable<Comment> addComment(@NonNull String parentId, @NonNull String text) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireUserAccessToken().flatMap(token ->
        mAPI.addComment(parentId, text)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map(response -> response.body().getComment())
    );
  }

  @Override
  public Observable<ListingResponse> getInbox(@NonNull String show, @Nullable String after) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.getInbox(show, after)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()));
  }

  @Override
  public Observable<Void> markAllMessagesRead() {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.markAllMessagesRead()
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()));
  }

  @Override
  public Observable<Void> markMessagesRead(@NonNull String commaSeparatedFullnames) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.markMessagesRead(commaSeparatedFullnames)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    );
  }

  @Override
  public Observable<Void> markMessagesUnread(@NonNull String commaSeparatedFullnames) {
    if (!connectedToNetwork()) return Observable.error(new NetworkUnavailableException());
    return requireAccessToken().flatMap(token ->
        mAPI.markMessagesUnread(commaSeparatedFullnames)
            .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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

  private boolean connectedToNetwork() {
    return AndroidUtils.isConnectedToNetwork(
        HoldTheNarwhal.getContext());
  }

  private Observable<Response<ResponseBody>> checkForUserRequiredError(
      Response<ResponseBody> response) {
    if (response.message().contains("USER_REQUIRED")) {
      return Observable.error(new RuntimeException("Sign in required"));
    } else return Observable.just(response);
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
