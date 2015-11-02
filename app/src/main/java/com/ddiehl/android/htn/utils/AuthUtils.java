package com.ddiehl.android.htn.utils;

import android.net.Uri;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.io.RedditServiceAuth;

public class AuthUtils {
    public static String getUserAuthCodeFromRedirectUri(String url) {
        Uri uri = Uri.parse(url);
        String query = uri.getQuery();
        String[] params = query.split("&");

        // Verify state parameter is correct
        String returnedState = getValueFromQuery(params[0]);
        if (!returnedState.equals(RedditServiceAuth.STATE)) {
            HoldTheNarwhal.getLogger().e("STATE does not match: %s (EXPECTED: %s)",
                    returnedState, RedditServiceAuth.STATE);
            return null;
        }

        // If successfully authorized, params[1] will be a grant code
        // Otherwise, params[1] is an error message
        String name = getNameFromQuery(params[1]);
        if (name.equals("code")) {
            return getValueFromQuery(params[1]);
        } else { // User declined to authorize application, or an error occurred
            String error = getValueFromQuery(params[1]);
            HoldTheNarwhal.getLogger().e("Error during authorization flow: " + error);
            return null;
        }
    }

    private static String getNameFromQuery(String query) {
        return query.substring(0, query.indexOf("="));
    }

    private static String getValueFromQuery(String query) {
        return query.substring(query.indexOf("=") + 1);
    }
}
