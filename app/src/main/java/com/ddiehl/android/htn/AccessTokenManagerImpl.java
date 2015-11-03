package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.ddiehl.reddit.identity.UserAccessToken;

import java.util.Date;

public class AccessTokenManagerImpl implements AccessTokenManager {
    private static final String PREFS_USER_ACCESS_TOKEN = "prefs_user_access_token";
    private static final String PREFS_APPLICATION_ACCESS_TOKEN = "prefs_application_access_token";
    private static final String PREF_ACCESS_TOKEN = "pref_access_token";
    private static final String PREF_TOKEN_TYPE = "pref_token_type";
    private static final String PREF_EXPIRATION = "pref_expiration";
    private static final String PREF_SCOPE = "pref_scope";
    private static final String PREF_REFRESH_TOKEN = "pref_refresh_token";

    // Seconds within expiration we should try to retrieve a new auth token
    private static final int EXPIRATION_THRESHOLD = 60;

    private Logger mLogger = HoldTheNarwhal.getLogger();
    private Context mContext = AndroidContextProvider.getContext();
    private AccessToken mUserAccessToken;
    private AccessToken mApplicationAccessToken;

    private AccessTokenManagerImpl() {
        mUserAccessToken = getSavedUserAccessToken();
        mApplicationAccessToken = getSavedApplicationAccessToken();
    }

    @Override
    public boolean isUserAuthorized() {
        return hasUserAccessToken();
    }

    @Override
    public boolean hasUserAccessToken() {
        return getUserAccessToken() != null;
    }

    @Override
    public boolean hasValidUserAccessToken() {
        AccessToken token = getUserAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    @Override
    public boolean hasUserAccessRefreshToken() {
        AccessToken token = getUserAccessToken();
        return token != null && token.hasRefreshToken();
    }

    @Override
    public boolean hasValidApplicationAccessToken() {
        AccessToken token = getApplicationAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    @Override
    public boolean hasValidAccessToken() {
        return hasValidUserAccessToken() || hasValidApplicationAccessToken();
    }

    @Override
    public AccessToken getUserAccessToken() {
        if (mUserAccessToken == null) {
            mUserAccessToken = getSavedUserAccessToken();
        }
        return mUserAccessToken;
    }

    @Override
    public AccessToken getApplicationAccessToken() {
        if (mApplicationAccessToken == null) {
            mApplicationAccessToken = getSavedApplicationAccessToken();
        }
        return mApplicationAccessToken;
    }

    // /data/data/com.ddiehl.android.htn.debug/shared_prefs/prefs_user_access_token.xml
    private AccessToken getSavedUserAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);

        if (sp.contains(PREF_ACCESS_TOKEN)) {
            AccessToken token = new UserAccessToken();
            token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            token.setScope(sp.getString(PREF_SCOPE, null));
            token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
            return token;
        }

        return null;
    }

    private AccessToken getSavedApplicationAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);

        if (sp.contains(PREF_ACCESS_TOKEN)) {
            AccessToken token = new ApplicationAccessToken();
            token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            token.setScope(sp.getString(PREF_SCOPE, null));
            token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
            return token;
        }

        return null;
    }

    @Override
    public void saveUserAccessTokenResponse(AuthorizationResponse response) {
        if (mUserAccessToken == null) {
            mUserAccessToken = new UserAccessToken();
        }
        mUserAccessToken.setToken(response.getToken());
        mUserAccessToken.setTokenType(response.getTokenType());
        mUserAccessToken.setExpiration(response.getExpiresIn() * 1000 + new Date().getTime());
        mUserAccessToken.setScope(response.getScope());
        // When refreshing, we might not receive the refresh token in the response; don't delete it
        if (response.getRefreshToken() != null) {
            mUserAccessToken.setRefreshToken(response.getRefreshToken());
        }

        mLogger.d("--ACCESS TOKEN RESPONSE--");
        mLogger.d("Access Token: " + mUserAccessToken.getToken());
        mLogger.d("Refresh Token: " + mUserAccessToken.getRefreshToken());

        SharedPreferences sp = mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
        sp.edit()
                .putString(PREF_ACCESS_TOKEN, mUserAccessToken.getToken())
                .putString(PREF_TOKEN_TYPE, mUserAccessToken.getTokenType())
                .putLong(PREF_EXPIRATION, mUserAccessToken.getExpiration())
                .putString(PREF_SCOPE, mUserAccessToken.getScope())
                .putString(PREF_REFRESH_TOKEN, mUserAccessToken.getRefreshToken())
                .apply();
    }

    @Override
    public void saveApplicationAccessTokenResponse(AuthorizationResponse response) {
        mApplicationAccessToken = new ApplicationAccessToken();
        mApplicationAccessToken.setToken(response.getToken());
        mApplicationAccessToken.setTokenType(response.getTokenType());
        mApplicationAccessToken.setExpiration(response.getExpiresIn() * 1000 + new Date().getTime());
        mApplicationAccessToken.setScope(response.getScope());
        mApplicationAccessToken.setRefreshToken(response.getRefreshToken());

        mLogger.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
                mApplicationAccessToken.getToken(), mApplicationAccessToken.getRefreshToken()));

        SharedPreferences sp = mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
        sp.edit()
                .putString(PREF_ACCESS_TOKEN, mApplicationAccessToken.getToken())
                .putString(PREF_TOKEN_TYPE, mApplicationAccessToken.getTokenType())
                .putLong(PREF_EXPIRATION, mApplicationAccessToken.getExpiration())
                .putString(PREF_SCOPE, mApplicationAccessToken.getScope())
                .putString(PREF_REFRESH_TOKEN, mApplicationAccessToken.getRefreshToken())
                .apply();
    }

    @Override
    public void clearSavedUserAccessToken() {
        mUserAccessToken = null;
        mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    @Override
    public void clearSavedApplicationAccessToken() {
        mApplicationAccessToken = null;
        mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    ///////////////
    // Singleton //
    ///////////////

    private static AccessTokenManager _instance;

    public static AccessTokenManager getInstance() {
        if (_instance == null) {
            synchronized (AccessTokenManagerImpl.class) {
                if (_instance == null) {
                    _instance = new AccessTokenManagerImpl();
                }
            }
        }
        return _instance;
    }
}
