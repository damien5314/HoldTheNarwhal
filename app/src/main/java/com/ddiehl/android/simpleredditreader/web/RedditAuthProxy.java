package com.ddiehl.android.simpleredditreader.web;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.RedditReaderApplication;
import com.ddiehl.android.simpleredditreader.events.ApplicationAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizeApplicationEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.RefreshUserAccessTokenEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthCodeReceivedEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.utils.AuthUtils;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Date;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

public class RedditAuthProxy implements IRedditService {
    public static final String TAG = RedditAuthProxy.class.getSimpleName();

    public static final String CLIENT_ID = "***REMOVED***";
    public static final String RESPONSE_TYPE = "code";
    public static final String DURATION = "permanent";
    public static final String STATE = BaseUtils.getRandomString();
    public static final String REDIRECT_URI = "http://127.0.0.1/";
    public static final String SCOPE = "identity,mysubreddits,privatemessages,read,report,save,submit,vote";
    public static final String HTTP_AUTH_HEADER = AuthUtils.getHttpAuthHeader(CLIENT_ID, "");

    public static final String AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize.compact" +
            "?client_id=" + CLIENT_ID +
            "&response_type=" + RESPONSE_TYPE +
            "&duration=" + DURATION +
            "&state=" + STATE +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=" + SCOPE;

    // Seconds within expiration we should try to retrieve a new auth token
    private static final int EXPIRATION_THRESHOLD = 360;

    public static final String PREF_AUTH_TOKEN = "pref_auth_token";
    public static final String PREF_TOKEN_TYPE = "pref_token_type";
    public static final String PREF_EXPIRATION = "pref_expiration";
    public static final String PREF_SCOPE = "pref_scope";
    public static final String PREF_REFRESH_TOKEN = "pref_refresh_token";

    private static RedditAuthProxy _instance;

    private Context mContext;
    private RedditAuthAPI mAPI;
    private RedditService mService;
    private Bus mBus;

    private String mAuthToken;
    private String mTokenType;
    private Date mExpiration;
    private String mScope;
    private String mRefreshToken;

    private Object mQueuedEvent;

    private RedditAuthProxy(Context context) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mAPI = buildApi();
        mService = new RedditService(mContext);

        // Retrieve authorization state from shared preferences
        retrieveAuthToken();
        mService.setAuthToken(mAuthToken);
    }

    public static RedditAuthProxy getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditAuthProxy.class) {
                if (_instance == null) {
                    _instance = new RedditAuthProxy(context);
                }
            }
        }
        return _instance;
    }

    private RedditAuthAPI buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(RedditEndpoint.NORMAL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditReaderApplication.USER_AGENT);
                        request.addHeader("Authorization", HTTP_AUTH_HEADER);
                    }
                })
                .build();

        return restAdapter.create(RedditAuthAPI.class);
    }

    public void saveAuthToken(AuthTokenResponse response) {
        String authToken = response.getAuthToken();
        if (authToken == null)
            return;

        mAuthToken = authToken;
        mTokenType = response.getTokenType();
        long expiresIn = response.getExpiresIn();
        mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
        mScope = response.getScope();

        String refreshToken = response.getRefreshToken();
        if (refreshToken != null) {
            mRefreshToken = refreshToken;
        }

        Log.d(TAG, "--AUTH TOKEN RESPONSE--");
        Log.d(TAG, "Access Token: " + mAuthToken);
        Log.d(TAG, "Refresh Token: " + mRefreshToken);

        mService.setAuthToken(mAuthToken);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit()
                .putString(PREF_AUTH_TOKEN, mAuthToken)
                .putString(PREF_TOKEN_TYPE, mTokenType)
                .putLong(PREF_EXPIRATION, mExpiration.getTime())
                .putString(PREF_SCOPE, mScope)
                .putString(PREF_REFRESH_TOKEN, mRefreshToken)
                .apply();
    }

    public void retrieveAuthToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAuthToken = sp.getString(PREF_AUTH_TOKEN, null);
        mTokenType = sp.getString(PREF_TOKEN_TYPE, null);
        long expirationTime = sp.getLong(PREF_EXPIRATION, 0);
        mExpiration = new Date(expirationTime);
        mScope = sp.getString(PREF_SCOPE, null);
        mRefreshToken = sp.getString(PREF_REFRESH_TOKEN, null);
    }

    public boolean hasValidAuthToken() {
        return mAuthToken != null && secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public long secondsUntilExpiration() {
        return Math.max(0, (mExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    @Subscribe
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = RedditPreferences.getInstance(mContext).getDeviceId();

        mAPI.getApplicationAuthToken(grantType, deviceId, new Callback<AuthTokenResponse>() {
            @Override
            public void success(AuthTokenResponse authTokenResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new ApplicationAuthorizedEvent(authTokenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new ApplicationAuthorizedEvent(error));
            }
        });
    }

    @Subscribe
    public void onApplicationAuthorized(ApplicationAuthorizedEvent event) {
        if (!event.isFailed()) {
            Toast.makeText(mContext, "Application authorized", Toast.LENGTH_SHORT).show();
            AuthTokenResponse response = event.getResponse();
            saveAuthToken(response);
            if (mQueuedEvent != null) {
                Object e = mQueuedEvent;
                mQueuedEvent = null;
                mBus.post(e);
            }
        }
    }

    /**
     * Retrieves access token when an authorization code is received
     */
    @Subscribe
    public void onUserAuthCodeReceived(UserAuthCodeReceivedEvent event) {
        String grantType = "authorization_code";
        String authCode = event.getCode();

        mAPI.getUserAuthToken(grantType, authCode, RedditAuthProxy.REDIRECT_URI,
                new Callback<AuthTokenResponse>() {
                    @Override
                    public void success(AuthTokenResponse authTokenResponse, Response response) {
                        BaseUtils.printResponseStatus(response);
                        mBus.post(new UserAuthorizedEvent(authTokenResponse));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        BaseUtils.showError(mContext, error);
                        BaseUtils.printResponse(error.getResponse());
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

        mAPI.refreshUserAuthToken(grantType, refreshToken, new Callback<AuthTokenResponse>() {
            @Override
            public void success(AuthTokenResponse authTokenResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mBus.post(new UserAuthorizedEvent(authTokenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new UserAuthorizedEvent(error));
            }
        });
    }

    @Subscribe
    public void onUserAuthorized(UserAuthorizedEvent event) {
        if (!event.isFailed()) {
            Toast.makeText(mContext, "User authorized", Toast.LENGTH_SHORT).show();
            AuthTokenResponse response = event.getResponse();
            saveAuthToken(response);
            if (mQueuedEvent != null) {
                Object e = mQueuedEvent;
                mQueuedEvent = null;
                mBus.post(e);
            }
        }
    }

    /**
     * Retrieves link listings for subreddit
     */
    @Subscribe
    public void onLoadLinks(LoadLinksEvent event) {
        if (!hasValidAuthToken()) {
            mQueuedEvent = event;
            if (mRefreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(mRefreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mService.onLoadLinks(event);
        }
    }

    /**
     * Retrieves comment listings for link passed as parameter
     */
    @Subscribe
    public void onLoadComments(LoadCommentsEvent event) {
        if (!hasValidAuthToken()) {
            mQueuedEvent = event;
            mBus.post(new AuthorizeApplicationEvent());
        } else {
            mService.onLoadComments(event);
        }
    }

    /**
     * Submits a vote on a link or comment
     */
    @Subscribe
    public void onVote(VoteEvent event) {
        if (!hasValidAuthToken()) {
            mQueuedEvent = event;
            mBus.post(new AuthorizeApplicationEvent());
        } else {
            mService.onVote(event);
        }
    }
}
