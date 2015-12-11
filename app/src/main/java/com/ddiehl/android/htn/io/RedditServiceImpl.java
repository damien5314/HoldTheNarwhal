package com.ddiehl.android.htn.io;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.analytics.Analytics;
import com.ddiehl.android.htn.events.requests.GetSubredditInfoEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.HideSubmittedEvent;
import com.ddiehl.android.htn.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.htn.events.responses.SubredditInfoLoadedEvent;
import com.ddiehl.android.htn.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.reddit.Hideable;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.adapters.AbsCommentDeserializer;
import com.ddiehl.reddit.adapters.ListingDeserializer;
import com.ddiehl.reddit.adapters.ListingResponseDeserializer;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.Friend;
import com.ddiehl.reddit.identity.FriendInfo;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.identity.UserSettings;
import com.ddiehl.reddit.listings.AbsComment;
import com.ddiehl.reddit.listings.CommentStub;
import com.ddiehl.reddit.listings.Link;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.ListingResponse;
import com.ddiehl.reddit.listings.MoreChildrenResponse;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;
import com.squareup.otto.Bus;

import java.io.File;
import java.util.List;
import java.util.Map;

import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RedditServiceImpl implements RedditService {
    private Bus mBus = BusProvider.getInstance();
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
        client.networkInterceptors().add((chain) -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "bearer " + mAccessTokenManager.getValidAccessToken())
                    .build();
            return chain.proceed(newRequest);
        });
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
        return mAPI.getUserSettings()
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body);
    }

    @Override
    public Observable<ResponseBody> updateUserSettings(Map<String, String> settings) {
        String json = new Gson().toJson(settings);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        return mAPI.updateUserSettings(body)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body);
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
        return mAPI.getComments(subreddit, article, sort, commentId)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body);
    }

    @Override
    public Observable<Pair<CommentStub, MoreChildrenResponse>> loadMoreChildren(
            @NonNull Link link, @NonNull CommentStub parentStub,
            @NonNull List<String> children, @Nullable String sort) {
        StringBuilder b = new StringBuilder();
        for (String child : children) b.append(child).append(",");
        String childrenString = b.toString();
        childrenString = childrenString.substring(0, Math.max(childrenString.length() - 1, 0));
        return mAPI.getMoreChildren(link.getName(), childrenString, sort)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> Pair.create(parentStub, response.body()));
    }

    @Override
    public Observable<UserIdentity> getUserInfo(@NonNull String username) {
        return mAPI.getUserInfo(username)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> response.body().getUser());
    }

    @Override
    public Observable<FriendInfo> getFriendInfo(String username) {
        return mAPI.getFriendInfo(username)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body);
    }

    @Override
    public Observable<List<Listing>> getUserTrophies(@NonNull String username) {
        return mAPI.getUserTrophies(username)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> response.body().getData().getTrophies());
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
        return requireUserAccessToken().flatMap(token -> mAPI.addFriend(username, body)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body));
    }

    @Override
    public Observable<ResponseBody> deleteFriend(@NonNull String username) {
        return requireUserAccessToken().flatMap(token -> mAPI.deleteFriend(username)
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
        return mAPI.addFriend(username, RequestBody.create(MediaType.parse("application/json"), json))
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(Response::body);
    }

    @Override
    public void onGetSubredditInfo(@NonNull GetSubredditInfoEvent event) {
        final String name = event.getSubredditName();

        mAPI.getSubredditInfo(name)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        subreddit -> mBus.post(new SubredditInfoLoadedEvent(subreddit.body())),
                        error -> {
                            mBus.post(error);
                            mBus.post(new SubredditInfoLoadedEvent(error));
                        }
                );
    }

    @Override
    public void onVote(@NonNull final VoteEvent event) {
        final Votable listing = event.getListing();
        String fullname = String.format("%s_%s", event.getType(), listing.getId());

        mAPI.vote(fullname, event.getDirection())
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (!response.message().contains("USER_REQUIRED")) {
                                mBus.post(new VoteSubmittedEvent(listing, event.getDirection()));
                            }
                        },
                        error -> {
                            mBus.post(error);
                            mBus.post(new VoteSubmittedEvent(error));
                        });
    }

    @Override
    public void onSave(@NonNull final SaveEvent event) {
        final Savable listing = event.getListing();
        final String category = event.getCategory();
        final boolean toSave = event.isToSave();

        Action1<Response> onSaveSuccess = response -> {
            if (!response.message().contains("USER_REQUIRED")) {
                mBus.post(new SaveSubmittedEvent(listing, category, toSave));
            }
        };

        Action1<Throwable> onSaveFailure = (error) -> {
            mBus.post(error);
            mBus.post(new SaveSubmittedEvent(error));
        };

        if (event.isToSave()) { // Save
            mAPI.save(listing.getName(), event.getCategory())
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSaveSuccess, onSaveFailure);
        } else { // Unsave
            mAPI.unsave(listing.getName())
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSaveSuccess, onSaveFailure);
        }
    }

    @Override
    public void onHide(@NonNull final HideEvent event) {
        final Hideable listing = event.getListing();
        final boolean toHide = event.isToHide();

        Action1<Response> onSuccess = (response) -> {
            if (!response.message().contains("USER_REQUIRED")) {
                mBus.post(new HideSubmittedEvent(listing, toHide));
            }
        };

        Action1<Throwable> onFailure = (error) -> {
            mBus.post(error);
            mBus.post(new HideSubmittedEvent(error));
        };

        if (event.isToHide()) { // Hide
            mAPI.hide(listing.getName())
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onFailure);
        } else { // Unhide
            mAPI.unhide(listing.getName())
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onSuccess, onFailure);
        }
    }

    @Override
    public void onReport(@NonNull final ReportEvent event) {

    }

    private Observable<AccessToken> requireAccessToken() {
        return mAccessTokenManager.getAccessToken()
                .doOnError(error -> mLogger.e("No access token available"));
    }

    private Observable<AccessToken> requireUserAccessToken() {
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
