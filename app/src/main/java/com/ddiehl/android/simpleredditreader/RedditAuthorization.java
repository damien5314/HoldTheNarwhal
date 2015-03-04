package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.view.WebViewActivity;
import com.ddiehl.android.simpleredditreader.web.AccessTokenResponse;

import java.util.Date;
import java.util.UUID;

public class RedditAuthorization {
    public static final String TAG = RedditAuthorization.class.getSimpleName();

    public static final String CLIENT_ID = "***REMOVED***";
    public static final String RESPONSE_TYPE = "code";
    public static final String DURATION = "permanent";
    public static final String STATE = getRandomString();
    public static final String REDIRECT_URI = "http://127.0.0.1/";
    public static final String SCOPE = "mysubreddits,privatemessages,read,report,save,submit,vote";
    public static final String HTTP_AUTH_HEADER = Utils.getHttpAuthHeader(CLIENT_ID, "");

    public static final String AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize" +
            "?client_id=" + CLIENT_ID +
            "&response_type=" + RESPONSE_TYPE +
            "&duration=" + DURATION +
            "&state=" + STATE +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=" + SCOPE;

    // Seconds within expiration we should try to retrieve a new access token
    private static final int EXPIRATION_THRESHOLD = 360;

    public static final String PREF_ACCESS_TOKEN = "pref_access_token";
    public static final String PREF_TOKEN_TYPE = "pref_token_type";
    public static final String PREF_EXPIRATION = "pref_expiration";
    public static final String PREF_SCOPE = "pref_scope";

    private static RedditAuthorization _instance;

    private Context mContext;
    private boolean mIsAuthorized = false;
    private String mState;
    private String mCode;
    private String mError;
    private String mAccessToken;
    private String mTokenType;
    private Date mExpiration;
    private String mScope;

    private RedditAuthorization(Context context) {
        mContext = context.getApplicationContext();
    }

    public static RedditAuthorization getInstance(Context context) {
        if (_instance == null) {
            synchronized (RedditAuthorization.class) {
                if (_instance == null) {
                    _instance = new RedditAuthorization(context);
                }
            }
        }
        return _instance;
    }

    public Intent getUserAuthorizationIntent() {
        Intent intent = new Intent(mContext, WebViewActivity.class);
        mState = getRandomString();
        Uri uri = Uri.parse(AUTHORIZATION_URL);
        intent.setData(uri);
        return intent;
    }

    public void saveAuthorizationCode(String url) {
        Uri uri = Uri.parse(url);
        Log.d(TAG, "URI: " + uri.toString());
        String query = uri.getQuery();
        String[] params = query.split("&");

        // Verify state parameter is correct
        mState = getValueFromQuery(params[0]);
        if (!mState.equals(STATE)) {
            Log.e(TAG, "STATE in response does not match request: " + mState);
            return;
        }

        // If successfully authorized, params[1] will be a grant code
        // Otherwise, params[1] is an error message
        String name = getNameFromQuery(params[1]);
        if (name.equals("code")) {
            mCode = getValueFromQuery(params[1]);
        } else { // User declined to authorize application, or an error occurred
            mError = getValueFromQuery(params[1]);
        }
    }

    public void saveAccessToken(AccessTokenResponse response) {
        if (response.getAccessToken() == null)
            return;

        mAccessToken = response.getAccessToken();
        mTokenType = response.getTokenType();
        long expiresIn = response.getExpiresIn();
        mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
        mScope = response.getScope();

        mIsAuthorized = true;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit()
                .putString(PREF_ACCESS_TOKEN, mAccessToken)
                .putString(PREF_TOKEN_TYPE, mTokenType)
                .putLong(PREF_EXPIRATION, mExpiration.getTime())
                .putString(PREF_SCOPE, mScope)
                .apply();
    }

    public void retrieveAccessToken() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        mAccessToken = sp.getString(PREF_ACCESS_TOKEN, null);
        mTokenType = sp.getString(PREF_TOKEN_TYPE, null);
        long expirationTime = sp.getLong(PREF_EXPIRATION, 0);
        mExpiration = new Date(expirationTime);
        mScope = sp.getString(PREF_SCOPE, null);

        if (mAccessToken != null) {
            mIsAuthorized = true;
        }
    }

    public boolean hasValidAccessToken() {
        if (mAccessToken == null) {
            retrieveAccessToken();
        }
        return mIsAuthorized && secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    public String getAuthHeader() {
        if (mAccessToken != null) {
            return "bearer " + mAccessToken;
        } else {
            return HTTP_AUTH_HEADER;
        }
    }

    public long secondsUntilExpiration() {
        return Math.max(0, (mExpiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    private static String getNameFromQuery(String query) {
        return query.substring(0, query.indexOf("="));
    }

    private static String getValueFromQuery(String query) {
        return query.substring(query.indexOf("=") + 1);
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString();
    }
}
