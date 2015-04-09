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
import com.ddiehl.android.simpleredditreader.events.GetUserIdentityEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.RefreshUserAccessTokenEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthCodeReceivedEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthorizationRefreshedEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.UserIdentityRetrievedEvent;
import com.ddiehl.android.simpleredditreader.events.UserIdentityUpdatedEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.model.UserIdentity;
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
    private static final int EXPIRATION_THRESHOLD = 60;

    private static final String PREF_AUTH_TOKEN = "pref_auth_token";
    private static final String PREF_TOKEN_TYPE = "pref_token_type";
    private static final String PREF_EXPIRATION = "pref_expiration";
    private static final String PREF_SCOPE = "pref_scope";
    private static final String PREF_REFRESH_TOKEN = "pref_refresh_token";
    private static final String PREF_IS_USER_ACCESS_TOKEN = "pref_is_user_access_token";

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
    private boolean mIsUserAccessToken = false;

    private Object mQueuedEvent;

    private static final String PREFS_USER_IDENTITY = "user_identity";
    private static final String PREF_HAS_MAIL = "pref_has_mail";
    private static final String PREF_NAME = "pref_name";
    private static final String PREF_CREATED = "pref_created";
    private static final String PREF_HIDE_FROM_ROBOTS = "pref_hide_from_robots";
    private static final String PREF_GOLD_CREDDITS = "pref_gold_creddits";
    private static final String PREF_CREATED_UTC = "pref_created_utc";
    private static final String PREF_HAS_MOD_MAIL = "pref_has_mod_mail";
    private static final String PREF_LINK_KARMA = "pref_link_karma";
    private static final String PREF_COMMENT_KARMA = "pref_comment_karma";
    private static final String PREF_IS_OVER_18 = "pref_is_over_18";
    private static final String PREF_IS_GOLD = "pref_is_gold";
    private static final String PREF_IS_MOD = "pref_is_mod";
    private static final String PREF_GOLD_EXPIRATION = "pref_gold_expiration";
    private static final String PREF_HAS_VERIFIED_EMAIL = "pref_has_verified_email";
    private static final String PREF_ID = "pref_id";
    private static final String PREF_INBOX_COUNT = "pref_inbox_count";

    private UserIdentity mUserIdentity;

    private RedditAuthProxy(Context context) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mAPI = buildApi();
        mService = new RedditService(mContext);

        retrieveSavedAuthToken();
        mService.setAuthToken(mAuthToken);

        retrieveSavedIdentity();
        mBus.post(new UserIdentityUpdatedEvent(mUserIdentity));

        if (hasValidUserAccessToken() && mUserIdentity == null) {
            mBus.post(new GetUserIdentityEvent());
        }
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

    public boolean hasValidAuthToken() {
        return mAuthToken != null && secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public boolean hasValidUserAccessToken() {
        return mAuthToken != null && mIsUserAccessToken && secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    private long secondsUntilExpiration() {
        return Math.max(0, (mExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    private void retrieveSavedAuthToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAuthToken = sp.getString(PREF_AUTH_TOKEN, null);
        mTokenType = sp.getString(PREF_TOKEN_TYPE, null);
        long expirationTime = sp.getLong(PREF_EXPIRATION, 0);
        mExpiration = new Date(expirationTime);
        mScope = sp.getString(PREF_SCOPE, null);
        mRefreshToken = sp.getString(PREF_REFRESH_TOKEN, null);
        mIsUserAccessToken = sp.getBoolean(PREF_IS_USER_ACCESS_TOKEN, false);
    }

    private void saveAuthToken(AuthTokenResponse response, boolean isUserAccessToken) {
        String authToken = response.getAuthToken();
        if (authToken == null)
            return;

        mAuthToken = authToken;
        mTokenType = response.getTokenType();
        long expiresIn = response.getExpiresIn();
        mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
        mScope = response.getScope();
        mIsUserAccessToken = isUserAccessToken;

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
                .putBoolean(PREF_IS_USER_ACCESS_TOKEN, mIsUserAccessToken)
                .apply();
    }

    private UserIdentity retrieveSavedIdentity() {
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);

        if (prefs.contains(PREF_ID)) {
            mUserIdentity = new UserIdentity();
            mUserIdentity.hasMail(prefs.getBoolean(PREF_HAS_MAIL, false));
            mUserIdentity.setName(prefs.getString(PREF_NAME, null));
            mUserIdentity.setCreated(prefs.getLong(PREF_CREATED, new Date().getTime()));
            mUserIdentity.isHiddenFromRobots(prefs.getBoolean(PREF_HIDE_FROM_ROBOTS, false));
            mUserIdentity.setGoldCreddits(prefs.getInt(PREF_GOLD_CREDDITS, 0));
            mUserIdentity.setCreatedUTC(prefs.getLong(PREF_CREATED_UTC, new Date().getTime()));
            mUserIdentity.hasModMail(prefs.getBoolean(PREF_HAS_MOD_MAIL, false));
            mUserIdentity.setLinkKarma(prefs.getInt(PREF_LINK_KARMA, 0));
            mUserIdentity.setCommentKarma(prefs.getInt(PREF_COMMENT_KARMA, 0));
            mUserIdentity.isOver18(prefs.getBoolean(PREF_IS_OVER_18, false));
            mUserIdentity.isGold(prefs.getBoolean(PREF_IS_GOLD, false));
            mUserIdentity.isMod(prefs.getBoolean(PREF_IS_MOD, false));
            mUserIdentity.setGoldExpiration(prefs.getLong(PREF_GOLD_EXPIRATION, 0));
            mUserIdentity.hasVerifiedEmail(prefs.getBoolean(PREF_HAS_VERIFIED_EMAIL, false));
            mUserIdentity.setId(prefs.getString(PREF_ID, null));
            mUserIdentity.setInboxCount(prefs.getInt(PREF_INBOX_COUNT, 0));
        }

        return mUserIdentity;
    }

    private void saveUserIdentity(UserIdentity identity) {
        Boolean hasMail = identity.hasMail();
        String name = identity.getName();
        Long created = identity.getCreated();
        Boolean isHiddenFromRobots = identity.isHiddenFromRobots();
        Integer goldCreddits = identity.getGoldCreddits();
        Long createdUTC = identity.getCreatedUTC();
        Boolean hasModMail = identity.hasModMail();
        Integer linkKarma = identity.getLinkKarma();
        Integer commentKarma = identity.getCommentKarma();
        Boolean isOver18 = identity.isOver18();
        Boolean isGold = identity.isGold();
        Boolean isMod = identity.isMod();
        Long goldExpiration = identity.getGoldExpiration();
        Boolean hasVerifiedEmail = identity.hasVerifiedEmail();
        String id = identity.getId();
        Integer inboxCount = identity.getInboxCount();

        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(PREF_HAS_MAIL, hasMail)
                .putString(PREF_NAME, name)
                .putLong(PREF_CREATED, created)
                .putBoolean(PREF_HIDE_FROM_ROBOTS, isHiddenFromRobots)
                .putInt(PREF_GOLD_CREDDITS, goldCreddits)
                .putLong(PREF_CREATED_UTC, createdUTC)
                .putBoolean(PREF_HAS_MOD_MAIL, hasModMail)
                .putInt(PREF_LINK_KARMA, linkKarma)
                .putInt(PREF_COMMENT_KARMA, commentKarma)
                .putBoolean(PREF_IS_OVER_18, isOver18)
                .putBoolean(PREF_IS_GOLD, isGold)
                .putBoolean(PREF_IS_MOD, isMod)
                .putLong(PREF_GOLD_EXPIRATION, goldExpiration != null ? goldExpiration : 0)
                .putBoolean(PREF_HAS_VERIFIED_EMAIL, hasVerifiedEmail)
                .putString(PREF_ID, id)
                .putInt(PREF_INBOX_COUNT, inboxCount)
                .apply();
    }

    private void clearUserIdentity() {
        mUserIdentity = null;
        SharedPreferences prefs = mContext.getSharedPreferences(PREFS_USER_IDENTITY, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Subscribe
    public void onUserIdentityRetrieved(UserIdentityRetrievedEvent event) {
        if (event.isFailed()) {
            Log.d(TAG, "Error retrieving user identity");
            return;
        }

        mUserIdentity = event.getUserIdentity();
        Toast.makeText(mContext, mUserIdentity.getName() + " authorized", Toast.LENGTH_LONG).show();
        saveUserIdentity(mUserIdentity);
        mBus.post(new UserIdentityUpdatedEvent(mUserIdentity));
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
        if (event.isFailed()) {
            return;
        }

        Toast.makeText(mContext, "Application authorized", Toast.LENGTH_SHORT).show();
        AuthTokenResponse response = event.getResponse();
        saveAuthToken(response, false);
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
                mBus.post(new UserAuthorizationRefreshedEvent(authTokenResponse));
            }

            @Override
            public void failure(RetrofitError error) {
                BaseUtils.showError(mContext, error);
                BaseUtils.printResponse(error.getResponse());
                mBus.post(new UserAuthorizationRefreshedEvent(error));
            }
        });
    }

    @Subscribe
    public void onUserAuthorized(UserAuthorizedEvent event) {
        if (event.isFailed()) {
            return;
        }

        Toast.makeText(mContext, "User authorized", Toast.LENGTH_SHORT).show();
        AuthTokenResponse response = event.getResponse();
        saveAuthToken(response, true);
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
            return;
        }

        Toast.makeText(mContext, "User authorization refreshed", Toast.LENGTH_SHORT).show();
        AuthTokenResponse response = event.getResponse();
        saveAuthToken(response, true);
        if (mUserIdentity == null) {
            mBus.post(new GetUserIdentityEvent());
        }
        if (mQueuedEvent != null) {
            Object e = mQueuedEvent;
            mQueuedEvent = null;
            mBus.post(e);
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
            if (mRefreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(mRefreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
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
            if (mRefreshToken != null) {
                mBus.post(new RefreshUserAccessTokenEvent(mRefreshToken));
            } else {
                mBus.post(new AuthorizeApplicationEvent());
            }
        } else {
            mService.onVote(event);
        }
    }
}
