package com.ddiehl.android.simpleredditreader.web;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.RedditReaderApplication;
import com.ddiehl.android.simpleredditreader.events.ApplicationAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.AuthorizeApplicationEvent;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthCodeReceivedEvent;
import com.ddiehl.android.simpleredditreader.events.UserAuthorizedEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingDeserializer;
import com.ddiehl.android.simpleredditreader.model.adapters.ListingResponseDeserializer;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
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
    public static final String SCOPE = "mysubreddits,privatemessages,read,report,save,submit,vote";
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

    private static RedditAuthProxy _instance;

    private Context mContext;
    private RedditApi mApi;
    private RedditEndpoint mEndpoint;
    private RedditService mService;
    private Bus mBus;

    private boolean mIsAuthorized = false;
    private String mAuthToken;
    private String mTokenType;
    private Date mExpiration;
    private String mScope;

    private Object mQueuedEvent;

    private RedditAuthProxy(Context context) {
        mContext = context.getApplicationContext();
        mBus = BusProvider.getInstance();
        mEndpoint = new RedditEndpoint();
        mEndpoint.setUrl(RedditEndpoint.AUTHORIZED);
        mApi = buildApi();
        mService = new RedditService(mContext, mApi);

        // Retrieve authorization state from shared preferences
        retrieveAuthToken();
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

    private RedditApi buildApi() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ListingResponse.class, new ListingResponseDeserializer())
                .registerTypeAdapter(Listing.class, new ListingDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(mEndpoint)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("User-Agent", RedditReaderApplication.USER_AGENT);
                        request.addHeader("Authorization", getAuthHeader());
                    }
                })
                .build();

        return restAdapter.create(RedditApi.class);
    }

    public void saveAuthToken(AuthTokenResponse response) {
        if (response.getAuthToken() == null)
            return;

        mAuthToken = response.getAuthToken();
        mTokenType = response.getTokenType();
        long expiresIn = response.getExpiresIn();
        mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
        mScope = response.getScope();

        mIsAuthorized = true;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit()
                .putString(PREF_AUTH_TOKEN, mAuthToken)
                .putString(PREF_TOKEN_TYPE, mTokenType)
                .putLong(PREF_EXPIRATION, mExpiration.getTime())
                .putString(PREF_SCOPE, mScope)
                .apply();
    }

    public void retrieveAuthToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAuthToken = sp.getString(PREF_AUTH_TOKEN, null);
        mTokenType = sp.getString(PREF_TOKEN_TYPE, null);
        long expirationTime = sp.getLong(PREF_EXPIRATION, 0);
        mExpiration = new Date(expirationTime);
        mScope = sp.getString(PREF_SCOPE, null);

        if (mAuthToken != null) {
            mIsAuthorized = true;
        }
    }

    public boolean hasValidAuthToken() {
        if (mAuthToken == null) {
            retrieveAuthToken();
        }
        return mIsAuthorized && secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public String getAuthHeader() {
        if (hasValidAuthToken()) {
            return "bearer " + mAuthToken;
        } else {
            return HTTP_AUTH_HEADER;
        }
    }

    public long secondsUntilExpiration() {
        return Math.max(0, (mExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    @Subscribe
    public void onAuthorizeApplication(AuthorizeApplicationEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String deviceId = RedditPreferences.getInstance(mContext).getDeviceId();

        mEndpoint.setUrl(RedditEndpoint.NORMAL);
        mApi.getApplicationAuthToken(grantType, deviceId, new Callback<AuthTokenResponse>() {
            @Override
            public void success(AuthTokenResponse authTokenResponse, Response response) {
                BaseUtils.printResponseStatus(response);
                mEndpoint.setUrl(RedditEndpoint.AUTHORIZED);
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
            AuthTokenResponse response = event.getResponse();
            saveAuthToken(response);
            if (mQueuedEvent != null) {
                mBus.post(mQueuedEvent);
            }
        }
    }

    /**
     * Notification that authentication state has changed
     */
    @Subscribe
    public void onUserAuthCodeReceived(UserAuthCodeReceivedEvent event) {
        String grantType = "https://oauth.reddit.com/grants/installed_client";
        String authCode = event.getCode();

        mEndpoint.setUrl(RedditEndpoint.NORMAL);
        // Retrieve auth token from authorization code
        mApi.getUserAuthToken(grantType, authCode, RedditAuthProxy.REDIRECT_URI,
                new Callback<AuthTokenResponse>() {
                    @Override
                    public void success(AuthTokenResponse response, Response response2) {
                        Log.d(TAG, "User auth token retrieved successfully");
                        BaseUtils.printResponse(response2);
//                        mBus.post(new UserAuthorizedEvent(response));
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "Error retrieving auth token for user");
                        BaseUtils.showError(mContext, error);
                        BaseUtils.printResponse(error.getResponse());
                        mBus.post(new UserAuthorizedEvent(error));
                    }
                });
    }

    @Subscribe
    public void onUserAuthorized(UserAuthorizedEvent event) {
        if (!event.isFailed()) {
            AuthTokenResponse response = event.getResponse();
            saveAuthToken(response);
            if (mQueuedEvent != null) {
                mBus.post(mQueuedEvent);
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
            mBus.post(new AuthorizeApplicationEvent());
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
