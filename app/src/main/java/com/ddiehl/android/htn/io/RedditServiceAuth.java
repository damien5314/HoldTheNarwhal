/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;


import android.content.Context;
import android.support.annotation.NonNull;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.AuthorizeApplicationEvent;
import com.ddiehl.android.htn.events.requests.FriendAddEvent;
import com.ddiehl.android.htn.events.requests.FriendDeleteEvent;
import com.ddiehl.android.htn.events.requests.FriendNoteSaveEvent;
import com.ddiehl.android.htn.events.requests.GetSubredditInfoEvent;
import com.ddiehl.android.htn.events.requests.GetUserIdentityEvent;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileListingEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileSummaryEvent;
import com.ddiehl.android.htn.events.requests.RefreshUserAccessTokenEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UpdateUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.ApplicationAuthorizedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthorizationRefreshedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthorizedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.utils.NUtils;
import com.ddiehl.reddit.identity.AccessToken;
import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RedditServiceAuth implements RedditService {

    public static final String CLIENT_ID = NUtils.getRedditClientId();
    public static final String RESPONSE_TYPE = "code";
    public static final String DURATION = "permanent";
    public static final String STATE = BaseUtils.getRandomString();
    public static final String REDIRECT_URI = "http://127.0.0.1/";
    public static final String SCOPE = "identity,mysubreddits,privatemessages,read,report,save," +
            "submit,vote,history,account,subscribe";
    public static final String HTTP_AUTH_HEADER = Credentials.basic(CLIENT_ID, "");

    public static final String AUTHORIZATION_URL =
            String.format("https://www.reddit.com/api/v1/authorize.compact" +
                    "?client_id=%s&response_type=%s&duration=%s&state=%s&redirect_uri=%s&scope=%s",
                    CLIENT_ID, RESPONSE_TYPE, DURATION, STATE, REDIRECT_URI, SCOPE);

    private Context mContext;
    private Bus mBus;
    private RedditAuthAPI mAuthAPI;
    private RedditServiceAPI mServiceAPI;
    private AccessTokenManager mAccessTokenManager;
    private IdentityManager mIdentityManager;

    private Object mQueuedEvent;

    private RedditServiceAuth(Context context) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mAccessTokenManager = AccessTokenManager.getInstance(mContext);
        mIdentityManager = IdentityManager.getInstance(mContext);
        mAuthAPI = buildApi();
        mServiceAPI = new RedditServiceAPI(mContext);
    }

    private RedditAuthAPI buildApi() {
        OkHttpClient client = new OkHttpClient();
        client.networkInterceptors().add(new UserAgentInterceptor(RedditService.USER_AGENT));
        client.networkInterceptors().add((chain) -> {
            Request originalRequest = chain.request();
            Request newRequest = originalRequest.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", HTTP_AUTH_HEADER)
                    .build();
            return chain.proceed(newRequest);
        });
        client.networkInterceptors().add(new LoggingInterceptor());
        client.networkInterceptors().add(new StethoInterceptor());

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit restAdapter = new Retrofit.Builder()
                .client(client)
                .baseUrl(ENDPOINT_NORMAL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        return restAdapter.create(RedditAuthAPI.class);
    }

    @Subscribe @SuppressWarnings("unused")
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = SettingsManager.getInstance(mContext).getDeviceId();

        mAuthAPI.getApplicationAuthToken(grantType, deviceId)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new ApplicationAuthorizedEvent(response.body())),
                        error -> {
                            mBus.post(error);
                            mBus.post(new ApplicationAuthorizedEvent(error));
                        });
    }

    @Subscribe @SuppressWarnings("unused")
    public void onApplicationAuthorized(ApplicationAuthorizedEvent event) {
        if (event.isFailed()) {
            mQueuedEvent = null;
            return;
        }

        mAccessTokenManager.saveApplicationAccessTokenResponse(event.getResponse());
        if (mQueuedEvent != null) {
            Object e = mQueuedEvent;
            mQueuedEvent = null;
            mBus.post(e);
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserAuthCodeReceived(UserAuthCodeReceivedEvent event) {
        String grantType = "authorization_code";
        String authCode = event.getCode();

        mAuthAPI.getUserAuthToken(grantType, authCode, RedditServiceAuth.REDIRECT_URI)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new UserAuthorizedEvent(response.body())),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserAuthorizedEvent(error));
                        });
    }

    @Subscribe @SuppressWarnings("unused")
    public void onRefreshUserAccessToken(RefreshUserAccessTokenEvent event) {
        String grantType = "refresh_token";
        String refreshToken = event.getRefreshToken();

        mAuthAPI.refreshUserAuthToken(grantType, refreshToken)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new UserAuthorizationRefreshedEvent(response.body())),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserAuthorizationRefreshedEvent(error));
                        });
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserSignOut(UserSignOutEvent event) {
        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null) {
            mAuthAPI.revokeUserAuthToken(token.getToken(), "access_token")
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {}, mBus::post);
            mAuthAPI.revokeUserAuthToken(token.getRefreshToken(), "refresh_token")
                    .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {}, mBus::post);
        }

        mAccessTokenManager.clearSavedUserAccessToken();
        mIdentityManager.clearSavedUserIdentity();
        mBus.post(new UserIdentitySavedEvent(null));
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserAuthorized(UserAuthorizedEvent event) {
        if (event.isFailed()) {
            mQueuedEvent = null;
            return;
        }

        mAccessTokenManager.saveUserAccessTokenResponse(event.getResponse());
        mIdentityManager.clearSavedUserIdentity();
        mBus.post(new GetUserIdentityEvent());
        if (mQueuedEvent != null) {
            Object e = mQueuedEvent;
            mQueuedEvent = null;
            mBus.post(e);
        }
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserAuthorizationRefreshed(UserAuthorizationRefreshedEvent event) {
        if (event.isFailed()) {
            mQueuedEvent = null;
            mAccessTokenManager.clearSavedUserAccessToken();
            mIdentityManager.clearSavedUserIdentity();
            return;
        }

        mAccessTokenManager.saveUserAccessTokenResponse(event.getResponse());
        mBus.post(new GetUserIdentityEvent()); // Refresh user identity in case it's out of date
        if (mQueuedEvent != null) {
            Object e = mQueuedEvent;
            mQueuedEvent = null;
            mBus.post(e);
        }
    }

    /////////////////////////////////////
    /////////// NO OAUTH SCOPE //////////
    /////////////////////////////////////

    @Subscribe @SuppressWarnings("unused") @Override
    public void onLoadLinks(@NonNull LoadSubredditEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onLoadLinks(event);
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onLoadLinkComments(@NonNull LoadLinkCommentsEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onLoadLinkComments(event);
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onLoadMoreChildren(@NonNull LoadMoreChildrenEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onLoadMoreChildren(event);
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onLoadUserProfileSummary(@NonNull LoadUserProfileSummaryEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onLoadUserProfileSummary(event);
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onLoadUserProfile(@NonNull LoadUserProfileListingEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onLoadUserProfile(event);
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onGetSubredditInfo(@NonNull GetSubredditInfoEvent event) {
        if (!mAccessTokenManager.hasValidAccessToken()) {
            mQueuedEvent = event;
            AccessToken userAccessToken = mAccessTokenManager.getUserAccessToken();
            String refreshToken = null;
            if (userAccessToken != null) {
                refreshToken = userAccessToken.getRefreshToken();
            }
            if (refreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(refreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mServiceAPI.onGetSubredditInfo(event);
        }
    }

    /////////////////////////////////////
    //////// REQUIRES OAUTH SCOPE ///////
    /////////////////////////////////////

    @Subscribe @SuppressWarnings("unused") @Override
    public void onGetUserIdentity(@NonNull GetUserIdentityEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onGetUserIdentity(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onGetUserSettings(@NonNull GetUserSettingsEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onGetUserSettings(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onUpdateUserSettings(@NonNull UpdateUserSettingsEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onUpdateUserSettings(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onVote(@NonNull VoteEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onVote(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onSave(@NonNull SaveEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onSave(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onHide(@NonNull HideEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onHide(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onReport(@NonNull ReportEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onReport(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onAddFriend(@NonNull FriendAddEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onAddFriend(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onDeleteFriend(@NonNull FriendDeleteEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onDeleteFriend(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    @Subscribe @SuppressWarnings("unused") @Override
    public void onSaveFriendNote(@NonNull FriendNoteSaveEvent event) {
        if (mAccessTokenManager.hasValidUserAccessToken()) {
            mServiceAPI.onSaveFriendNote(event);
            return;
        }

        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null && token.hasRefreshToken()) {
            mQueuedEvent = event;
            mBus.post(new RefreshUserAccessTokenEvent(token.getRefreshToken()));
        } else {
            mQueuedEvent = null;
        }
    }

    ///////////////
    // Singleton //
    ///////////////

    private static RedditServiceAuth _instance;

    public static RedditServiceAuth getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditServiceAuth.class) {
                if (_instance == null) {
                    _instance = new RedditServiceAuth(context);
                }
            }
        }
        return _instance;
    }
}
