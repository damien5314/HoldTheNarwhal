/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;


import android.content.Context;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.SettingsManager;
import com.ddiehl.android.htn.events.requests.AuthorizeApplicationEvent;
import com.ddiehl.android.htn.events.requests.FriendAddEvent;
import com.ddiehl.android.htn.events.requests.FriendDeleteEvent;
import com.ddiehl.android.htn.events.requests.FriendNoteSaveEvent;
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
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.android.schedulers.AndroidSchedulers;

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
        client.networkInterceptors().add(new StethoInterceptor());
        client.networkInterceptors().add(new LoggingInterceptor());

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint(ENDPOINT_NORMAL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(request -> {
                    request.addHeader("User-Agent", RedditService.USER_AGENT);
                    request.addHeader("Authorization", HTTP_AUTH_HEADER);
                })
                .build();

        return restAdapter.create(RedditAuthAPI.class);
    }

    @Subscribe
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = SettingsManager.getInstance(mContext).getDeviceId();

        mAuthAPI.getApplicationAuthToken(grantType, deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new ApplicationAuthorizedEvent(response)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new ApplicationAuthorizedEvent(error));
                        });
    }

    @Subscribe
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

    /**
     * Retrieves access token when an authorization code is received
     */
    @Subscribe
    public void onUserAuthCodeReceived(UserAuthCodeReceivedEvent event) {
        String grantType = "authorization_code";
        String authCode = event.getCode();

        mAuthAPI.getUserAuthToken(grantType, authCode, RedditServiceAuth.REDIRECT_URI)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new UserAuthorizedEvent(response)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserAuthorizedEvent(error));
                        });
    }

    /**
     * Retrieves access token when application has a refresh token available
     */
    @Subscribe
    public void onRefreshUserAccessToken(RefreshUserAccessTokenEvent event) {
        String grantType = "refresh_token";
        String refreshToken = event.getRefreshToken();

        mAuthAPI.refreshUserAuthToken(grantType, refreshToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> mBus.post(new UserAuthorizationRefreshedEvent(response)),
                        error -> {
                            mBus.post(error);
                            mBus.post(new UserAuthorizationRefreshedEvent(error));
                        });
    }

    /**
     * Invalidates an access token when a user requests sign out
     */
    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        AccessToken token = mAccessTokenManager.getUserAccessToken();
        if (token != null) {
            mAuthAPI.revokeUserAuthToken(token.getToken(), "access_token")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {}, mBus::post);
            mAuthAPI.revokeUserAuthToken(token.getRefreshToken(), "refresh_token")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {}, mBus::post);
        }

        mAccessTokenManager.clearSavedUserAccessToken();
        mIdentityManager.clearSavedUserIdentity();
        mBus.post(new UserIdentitySavedEvent(null));
    }

    @Subscribe
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

    @Subscribe
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

    @Subscribe @Override
    public void onLoadLinks(LoadSubredditEvent event) {
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

    @Subscribe @Override
    public void onLoadLinkComments(LoadLinkCommentsEvent event) {
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

    @Subscribe @Override
    public void onLoadMoreChildren(LoadMoreChildrenEvent event) {
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

    @Subscribe @Override
    public void onLoadUserProfileSummary(LoadUserProfileSummaryEvent event) {
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

    @Subscribe @Override
    public void onLoadUserProfile(LoadUserProfileListingEvent event) {
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

    /////////////////////////////////////
    //////// REQUIRES OAUTH SCOPE ///////
    /////////////////////////////////////

    @Override @Subscribe
    public void onGetUserIdentity(GetUserIdentityEvent event) {
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

    @Override @Subscribe
    public void onGetUserSettings(GetUserSettingsEvent event) {
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

    @Override @Subscribe
    public void onUpdateUserSettings(UpdateUserSettingsEvent event) {
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

    @Subscribe @Override
    public void onVote(VoteEvent event) {
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

    @Subscribe @Override
    public void onSave(SaveEvent event) {
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

    @Subscribe @Override
    public void onHide(HideEvent event) {
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

    @Subscribe @Override
    public void onReport(ReportEvent event) {
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

    @Subscribe @Override
    public void onAddFriend(FriendAddEvent event) {
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

    @Subscribe @Override
    public void onDeleteFriend(FriendDeleteEvent event) {
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

    @Subscribe @Override
    public void onSaveFriendNote(FriendNoteSaveEvent event) {
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
