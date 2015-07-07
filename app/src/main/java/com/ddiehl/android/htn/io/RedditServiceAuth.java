/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;


import android.content.Context;

import com.ddiehl.android.htn.AccessTokenManager;
import com.ddiehl.android.htn.BusProvider;
import com.ddiehl.android.htn.IdentityManager;
import com.ddiehl.android.htn.RedditPrefs;
import com.ddiehl.android.htn.events.requests.AuthorizeApplicationEvent;
import com.ddiehl.android.htn.events.requests.GetUserIdentityEvent;
import com.ddiehl.android.htn.events.requests.GetUserSettingsEvent;
import com.ddiehl.android.htn.events.requests.HideEvent;
import com.ddiehl.android.htn.events.requests.LoadLinkCommentsEvent;
import com.ddiehl.android.htn.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.htn.events.requests.LoadSubredditEvent;
import com.ddiehl.android.htn.events.requests.LoadUserProfileEvent;
import com.ddiehl.android.htn.events.requests.RefreshUserAccessTokenEvent;
import com.ddiehl.android.htn.events.requests.ReportEvent;
import com.ddiehl.android.htn.events.requests.SaveEvent;
import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.events.requests.VoteEvent;
import com.ddiehl.android.htn.events.responses.ApplicationAuthorizedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthCodeReceivedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthorizationRefreshedEvent;
import com.ddiehl.android.htn.events.responses.UserAuthorizedEvent;
import com.ddiehl.android.htn.events.responses.UserIdentitySavedEvent;
import com.ddiehl.android.htn.utils.AuthUtils;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.android.htn.utils.NUtils;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class RedditServiceAuth implements RedditService {

    public static final String CLIENT_ID = NUtils.getRedditClientId();
    public static final String RESPONSE_TYPE = "code";
    public static final String DURATION = "permanent";
    public static final String STATE = BaseUtils.getRandomString();
    public static final String REDIRECT_URI = "http://127.0.0.1/";
    public static final String SCOPE = "identity,mysubreddits,privatemessages,read,report,save,submit,vote,history";
    public static final String HTTP_AUTH_HEADER = AuthUtils.getHttpAuthHeader(CLIENT_ID, "");

    public static final String AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize.compact" +
            "?client_id=" + CLIENT_ID +
            "&response_type=" + RESPONSE_TYPE +
            "&duration=" + DURATION +
            "&state=" + STATE +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=" + SCOPE;

    private static RedditServiceAuth _instance;

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
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_NORMAL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditService.USER_AGENT);
                        request.addHeader("Authorization", HTTP_AUTH_HEADER);
                    }
                })
                .build();

        return restAdapter.create(RedditAuthAPI.class);
    }

    @Subscribe
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = RedditPrefs.getInstance(mContext).getDeviceId();

        mAuthAPI.getApplicationAuthToken(grantType, deviceId, new Callback<AuthorizationResponse>() {
            @Override
            public void success(AuthorizationResponse response, Response response1) {
                BaseUtils.printResponseStatus(response1);
                mBus.post(new ApplicationAuthorizedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new ApplicationAuthorizedEvent(error));
            }
        });
    }

    @Subscribe
    public void onApplicationAuthorized(ApplicationAuthorizedEvent event) {
        if (event.isFailed()) {
            mQueuedEvent = null;
            return;
        }

//        Toast.makeText(mContext, R.string.application_authorized, Toast.LENGTH_SHORT).show();
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

        mAuthAPI.getUserAuthToken(grantType, authCode, RedditServiceAuth.REDIRECT_URI,
                new Callback<AuthorizationResponse>() {
                    @Override
                    public void success(AuthorizationResponse response, Response response1) {
                        BaseUtils.printResponseStatus(response1);
                        mBus.post(new UserAuthorizedEvent(response));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BaseUtils.printResponse(error.getResponse());
                        mBus.post(error);
                        mBus.post(new UserAuthorizedEvent(error));
                    }
                });
    }

    /**
     * Retrieves access token when application has a refresh token available
     */
    @Subscribe
    public void onRefreshUserAccessToken(RefreshUserAccessTokenEvent event) {
        String grantType = "refresh_token";
        String refreshToken = event.getRefreshToken();

        mAuthAPI.refreshUserAuthToken(grantType, refreshToken, new Callback<AuthorizationResponse>() {
            @Override
            public void success(AuthorizationResponse response, Response response1) {
                BaseUtils.printResponseStatus(response1);
                mBus.post(new UserAuthorizationRefreshedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.printResponse(error.getResponse());
                mBus.post(error);
                mBus.post(new UserAuthorizationRefreshedEvent(error));
            }
        });
    }

    /**
     * Invalidates an access token when a user requests sign out
     */
    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        if (mAccessTokenManager.getUserAccessToken() != null) {
            mAuthAPI.revokeUserAuthToken(mAccessTokenManager.getUserAccessToken().getToken(),
                    "access_token", new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
//                    Toast.makeText(mContext, R.string.access_token_revoked, Toast.LENGTH_SHORT).show();
                    BaseUtils.printResponseStatus(response);
                }

                @Override
                public void failure(RetrofitError error) {
                    mBus.post(error);
                    BaseUtils.printResponse(error.getResponse());
                }
            });

            mAuthAPI.revokeUserAuthToken(mAccessTokenManager.getUserAccessToken().getRefreshToken(),
                    "refresh_token", new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            BaseUtils.printResponseStatus(response);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            mBus.post(error);
                            BaseUtils.printResponse(error.getResponse());
                        }
                    });
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

//        Toast.makeText(mContext, R.string.user_authorized, Toast.LENGTH_SHORT).show();
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

//        Toast.makeText(mContext, R.string.user_authorization_refreshed, Toast.LENGTH_SHORT).show();
        mAccessTokenManager.saveUserAccessTokenResponse(event.getResponse());
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
    public void onLoadUserProfile(LoadUserProfileEvent event) {
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
//            mBus.post(new UserRequiredException());
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
//            mBus.post(new UserRequiredException());
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
//            mBus.post(new UserRequiredException());
        }
    }

    @Override
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
//            mBus.post(new UserRequiredException());
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
//            mBus.post(new UserRequiredException());
        }
    }

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
