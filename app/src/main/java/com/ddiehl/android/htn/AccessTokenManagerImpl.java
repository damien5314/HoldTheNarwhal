package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.events.requests.UserSignOutEvent;
import com.ddiehl.android.htn.io.RedditServiceAuth;
import com.ddiehl.android.htn.logging.Logger;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.squareup.otto.Subscribe;

import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    private RedditServiceAuth mServiceAuth = new RedditServiceAuth();
    private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();
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
        return getSavedUserAccessToken() != null;
    }

    @Override
    public boolean hasValidUserAccessToken() {
        AccessToken token = getSavedUserAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    @Override
    public boolean hasUserAccessRefreshToken() {
        AccessToken token = getSavedUserAccessToken();
        return token != null && token.hasRefreshToken();
    }

    @Override
    public boolean hasValidApplicationAccessToken() {
        AccessToken token = getSavedApplicationAccessToken();
        return token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD;
    }

    @Override
    public boolean hasValidAccessToken() {
        return hasValidUserAccessToken() || hasValidApplicationAccessToken();
    }

    @Override
    public void onUserAuthCodeReceived(String authCode) {
        String grantType = "authorization_code";
        mServiceAuth.getUserAuthToken(grantType, authCode, RedditServiceAuth.REDIRECT_URI)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            saveUserAccessTokenResponse(response);
                            mIdentityManager.clearSavedUserIdentity();
                        },
                        error -> {
                            mIdentityManager.clearSavedUserIdentity();
                        });
    }

    @Override
    public Observable<AccessToken> getUserAccessToken() {
        return Observable.create(subscriber -> {
            if (mUserAccessToken == null) {
                // Attempt to retrieve saved user access token
                mUserAccessToken = getSavedUserAccessToken();
            }
            if (mUserAccessToken != null) {
                // If we have one, check if the access token is expired
                if (mUserAccessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
                    // If not, return it
                    subscriber.onNext(mUserAccessToken);
                    subscriber.onCompleted();
                } else {
                    // Otherwise, check if we have a refresh token
                    final String refreshToken = mUserAccessToken.getRefreshToken();
                    if (refreshToken != null) {
                        // If so, ask RedditServiceAuth to refresh it
                        mServiceAuth.refreshUserAccessToken(refreshToken).subscribe(
                                authorizationResponse -> {
                                    // Save token and return it
                                    saveUserAccessTokenResponse(authorizationResponse);
                                    subscriber.onNext(mUserAccessToken);
                                }, error -> {
                                    // Clear token and trigger onError
                                    clearSavedUserAccessToken();
                                    mIdentityManager.clearSavedUserIdentity();
                                    subscriber.onError(error);
                                }, subscriber::onCompleted);
                    } else {
                        // Otherwise, clear the token and trigger onError
                        clearSavedUserAccessToken();
                        subscriber.onError(new RuntimeException("No refresh token available"));
                    }
                }
            } else {
                // If we don't have a saved access token, trigger onError
                subscriber.onError(new RuntimeException("No user access token available"));
            }
        });
    }

    @Override
    public Observable<AccessToken> getApplicationAccessToken() {
        return Observable.create(subscriber -> getUserAccessToken().subscribe(accessToken -> {
            // User access token should be used instead, if we have one available
            subscriber.onNext(accessToken);
            subscriber.onCompleted();
        }, error -> {
            // If there was an error from retrieving the user access token, we should
            // try to retrieve the application access token
            if (mApplicationAccessToken == null) {
                mApplicationAccessToken = getSavedApplicationAccessToken();
            }
            if (mApplicationAccessToken != null &&
                    mApplicationAccessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
                // If saved application access token is valid, return it
                subscriber.onNext(mApplicationAccessToken);
                subscriber.onCompleted();
            } else {
                // Otherwise, request RedditServiceAuth to retrieve a new one
                mServiceAuth.authorizeApplication()
                        .subscribe(authorizationResponse -> {
                            saveApplicationAccessTokenResponse(authorizationResponse);
                            subscriber.onNext(mApplicationAccessToken);
                        }, subscriber::onError, subscriber::onCompleted);
            }
        }));
    }

    // /data/data/com.ddiehl.android.htn.debug/shared_prefs/prefs_user_access_token.xml
    @Override
    public AccessToken getSavedUserAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(
                PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
        if (mUserAccessToken != null) return mUserAccessToken;
        if (sp.contains(PREF_ACCESS_TOKEN)) {
            mUserAccessToken = new UserAccessToken();
            mUserAccessToken.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            mUserAccessToken.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            mUserAccessToken.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            mUserAccessToken.setScope(sp.getString(PREF_SCOPE, null));
            mUserAccessToken.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
        }
        return mUserAccessToken;
    }

    @Override
    public AccessToken getSavedApplicationAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(
                PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
        if (mApplicationAccessToken != null) return mApplicationAccessToken;
        if (sp.contains(PREF_ACCESS_TOKEN)) {
            mApplicationAccessToken = new ApplicationAccessToken();
            mApplicationAccessToken.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
            mApplicationAccessToken.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
            mApplicationAccessToken.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
            mApplicationAccessToken.setScope(sp.getString(PREF_SCOPE, null));
            mApplicationAccessToken.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
        }
        return mApplicationAccessToken;
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

        mLogger.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
                mUserAccessToken.getToken(), mUserAccessToken.getRefreshToken()));

        SharedPreferences sp = mContext.getSharedPreferences(
                PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
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

        SharedPreferences sp = mContext.getSharedPreferences(
                PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
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

    @Subscribe @SuppressWarnings("unused")
    public void onUserSignOut(UserSignOutEvent event) {
        AccessToken token = getSavedUserAccessToken();
        if (token != null) {
            mServiceAuth.revokeAuthToken().call(token.getToken(), "access_token");
            mServiceAuth.revokeAuthToken().call(token.getRefreshToken(), "refresh_token");
        }

        clearSavedUserAccessToken();
        mIdentityManager.clearSavedUserIdentity();
//        mBus.post(new UserIdentitySavedEvent(null));
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
