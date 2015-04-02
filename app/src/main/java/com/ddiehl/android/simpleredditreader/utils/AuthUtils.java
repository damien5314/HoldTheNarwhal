package com.ddiehl.android.simpleredditreader.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ddiehl.android.simpleredditreader.view.WebViewActivity;
import com.ddiehl.android.simpleredditreader.web.RedditAuthProxy;

import java.util.UUID;

public class AuthUtils {
    private static final String TAG = AuthUtils.class.getSimpleName();

    public static Intent getUserAuthCodeIntent(Context context) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Uri uri = Uri.parse(RedditAuthProxy.AUTHORIZATION_URL);
        intent.setData(uri);
        return intent;
    }

    public static String getUserAuthCodeFromRedirectUri(String url) {
        Uri uri = Uri.parse(url);
        Log.d(TAG, "URI: " + uri.toString());
        String query = uri.getQuery();
        String[] params = query.split("&");

        // Verify state parameter is correct
        String returnedState = getValueFromQuery(params[0]);
        if (!returnedState.equals(RedditAuthProxy.STATE)) {
            Log.e(TAG, "STATE does not match: " + returnedState + " (EXPECTED: " + RedditAuthProxy.STATE + ")");
            return null;
        }

        // If successfully authorized, params[1] will be a grant code
        // Otherwise, params[1] is an error message
        String name = getNameFromQuery(params[1]);
        if (name.equals("code")) {
            return getValueFromQuery(params[1]);
        } else { // User declined to authorize application, or an error occurred
            String error = getValueFromQuery(params[1]);
            Log.e(TAG, "Error during authorization flow: " + error);
            return null;
        }
    }

    public static String getNameFromQuery(String query) {
        return query.substring(0, query.indexOf("="));
    }

    public static String getValueFromQuery(String query) {
        return query.substring(query.indexOf("=") + 1);
    }
}
