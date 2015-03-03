package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    // If expiration time is less than this config, isAuthorized returns false
    // Provides a buffer in case of lag between reddit servers and app client
    public static final int AUTHORIZATION_TIME_THRESHOLD = 10;

    private static AuthorizationState mAuthorizationState = AuthorizationState.Unauthorized;

    private static String mState;
    private static String mCode;
    private static String mError;

    private static String mAccessToken;
    private static String mTokenType;
    private static Date mExpiration;
    private static String mScope;

    private RedditAuthorization() { }

    public static Intent getAuthorizationIntent(Context context) {
        Intent intent = new Intent(context, WebViewActivity.class);
        mState = getRandomString();
        Uri uri = Uri.parse(AUTHORIZATION_URL);
        intent.setData(uri);
        return intent;
    }

    public static void saveAuthorizationCode(String url) {
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

    public static void saveAccessToken(AccessTokenResponse response) {
        mAuthorizationState = AuthorizationState.ApplicationAuthorized;

        mAccessToken = response.getAccessToken();
        mTokenType = response.getTokenType();
        long expiresIn = response.getExpiresIn();
        mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
        mScope = response.getScope();
    }

    public static AuthorizationState getAuthorizationState() {
        return mAuthorizationState;
    }

    public static String getAuthHeader() {
        if (mAccessToken != null) {
            return "bearer " + mAccessToken;
        } else {
            return HTTP_AUTH_HEADER;
        }
    }

    public static long secondsUntilExpiration() {
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
