package com.ddiehl.android.simpleredditreader;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.view.WebViewActivity;

import java.util.Date;
import java.util.UUID;

public class RedditAuthorization {
    public static final String TAG = RedditAuthorization.class.getSimpleName();

    public static final String CLIENT_ID = "***REMOVED***";
    public static final String RESPONSE_TYPE = "token";
    public static final String REDIRECT_URI = "http://127.0.0.1/";
    public static final String SCOPE = "mysubreddits,privatemessages,read,report,save,submit,vote";
    public static final String AUTHORIZATION_URL = "https://www.reddit.com/api/v1/authorize" +
            "?client_id=" + CLIENT_ID +
            "&response_type=" + RESPONSE_TYPE +
            "&state=" + getRandomString() +
            "&redirect_uri=" + REDIRECT_URI +
            "&scope=" + SCOPE;

    // If expiration time is less than this config, isAuthorized returns false
    // Provides a buffer in case of lag between reddit servers and app client
    public static final int AUTHORIZATION_TIME_THRESHOLD = 10;

    private static boolean mIsAuthorized = false;
    private static String mAccessToken;
    private static String mTokenType;
    private static String mState;
    private static Date mExpiration;
    private static String[] mScopes;
    private static String mError;

    public static Intent getAuthorizationIntent(Context context) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Uri uri = Uri.parse(AUTHORIZATION_URL);
        intent.setData(uri);
        return intent;
    }

    public static void saveAuthenticationState(String url) {
        Uri uri = Uri.parse(url);
        Log.d(TAG, "URI: " + uri.toString());
        String fragment = uri.getFragment();
        String[] params = fragment.split("&");

        // Expecting 5 parameters in a positive response
        if (params.length == 5) {
            mIsAuthorized = true;
            mAccessToken = params[0].substring(params[0].indexOf("=") + 1);
            mTokenType = params[1].substring(params[1].indexOf("=") + 1);
            mState = params[2].substring(params[2].indexOf("=") + 1);
            int expiresIn = Integer.valueOf(params[3].substring(params[3].indexOf("=") + 1));
            mExpiration = new Date(System.currentTimeMillis() + (expiresIn * 1000));
            mScopes = params[4].substring(params[4].indexOf("=") + 1).split("\\+");
        } else { // User declined to authorize application
            mIsAuthorized = false;
            mState = params[0].substring(params[0].indexOf("=") + 1);
            mError = params[1].substring(params[1].indexOf("=") + 1);
        }
    }

    public static boolean isAuthorized() {
        return mIsAuthorized && secondsUntilExpiration() > AUTHORIZATION_TIME_THRESHOLD;
    }

    public static long secondsUntilExpiration() {
        return (mExpiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString();
    }
}
