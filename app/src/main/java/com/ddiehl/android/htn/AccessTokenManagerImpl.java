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
import rx.functions.Action1;
import rx.functions.Func1;
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

    @Override
    public boolean isUserAuthorized() {
        return getSavedUserAccessToken() != null;
    }

    @Override
    public String getValidAccessToken() {
        AccessToken token;
        token = getSavedUserAccessToken();
        if (token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
            return token.getToken();
        }

        token = getSavedApplicationAccessToken();
        if (token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
            return token.getToken();
        }

        return null;
    }

    @Override
    public boolean hasValidAccessToken() {
        return getValidAccessToken() != null;
    }

    @Override
    public void onUserAuthCodeReceived(String authCode) {
        mIdentityManager.clearSavedUserIdentity();
        String grantType = "authorization_code";
        mServiceAuth.getUserAuthToken(grantType, authCode, RedditServiceAuth.REDIRECT_URI)
                .subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(responseToAccessToken())
                .subscribe(saveUserAccessToken());
    }

    @Override
    public Observable<AccessToken> getUserAccessToken() {
        return Observable.just(getSavedUserAccessToken())
                .flatMap(refreshUserAccessToken());
    }

    private Func1<AccessToken, Observable<AccessToken>> refreshUserAccessToken() {
        return (accessToken) -> {
            if (accessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
                return Observable.just(accessToken);
            } else return refreshUserAccessToken(accessToken);
        };
    }

    private Observable<AccessToken> refreshUserAccessToken(AccessToken accessToken) {
        String refreshToken = accessToken.getRefreshToken();
        if (refreshToken == null) {
            return Observable.error(new RuntimeException("No refresh token available"));
        }
        return mServiceAuth.refreshUserAccessToken(refreshToken)
                .map(responseToAccessToken())
                .doOnNext(saveUserAccessToken())
                .doOnError(error -> {
                    clearSavedUserAccessToken();
                    mIdentityManager.clearSavedUserIdentity();
                });
    }

    private Func1<AccessToken, Observable<AccessToken>> refreshApplicationAccessToken() {
        return accessToken -> {
            if (accessToken != null && accessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
                return Observable.just(accessToken);
            } else {
                return mServiceAuth.authorizeApplication()
                        .map(responseToAccessToken())
                        .doOnNext(saveApplicationAccessToken());
            }
        };
    }

    private Observable<AccessToken> getApplicationAccessToken() {
        return Observable.just(getSavedApplicationAccessToken());
    }

    @Override
    public Observable<AccessToken> getAccessToken() {
        return getUserAccessToken()
                .onErrorResumeNext(getApplicationAccessToken())
                .flatMap(refreshApplicationAccessToken());
    }

    private Func1<AuthorizationResponse, AccessToken> responseToAccessToken() {
        return response -> {
            AccessToken token = new UserAccessToken();
            token.setToken(response.getToken());
            token.setTokenType(response.getToken());
            token.setExpiration(response.getExpiresIn() * 1000 + new Date().getTime());
            token.setScope(response.getScope());
            token.setRefreshToken(response.getRefreshToken());
            return token;
        };
    }

    // /data/data/com.ddiehl.android.htn.debug/shared_prefs/prefs_user_access_token.xml
    @Override
    public AccessToken getSavedUserAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(
                PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
        if (!sp.contains(PREF_ACCESS_TOKEN)) return null;
        AccessToken token = new UserAccessToken();
        token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
        token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
        token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
        token.setScope(sp.getString(PREF_SCOPE, null));
        token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
        return token;
    }

    @Override
    public AccessToken getSavedApplicationAccessToken() {
        SharedPreferences sp =  mContext.getSharedPreferences(
                PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
        if (!sp.contains(PREF_ACCESS_TOKEN)) return null;
        AccessToken token = new ApplicationAccessToken();
        token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
        token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
        token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
        token.setScope(sp.getString(PREF_SCOPE, null));
        token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
        return token;
    }

    private Action1<AccessToken> saveUserAccessToken() {
        return token -> {
            mLogger.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
                    token.getToken(), token.getRefreshToken()));
            SharedPreferences sp =
                    mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
            sp.edit()
                    .putString(PREF_ACCESS_TOKEN, token.getToken())
                    .putString(PREF_TOKEN_TYPE, token.getTokenType())
                    .putLong(PREF_EXPIRATION, token.getExpiration())
                    .putString(PREF_SCOPE, token.getScope())
                    .apply();
            // Don't overwrite the refresh token if we didn't get a fresh one
            if (token.getRefreshToken() != null) {
                sp.edit().putString(PREF_REFRESH_TOKEN, token.getRefreshToken()).apply();
            }
        };
    }

    private Action1<AccessToken> saveApplicationAccessToken() {
        return token -> {
            mLogger.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
                    token.getToken(), token.getRefreshToken()));
            SharedPreferences sp = mContext.getSharedPreferences(
                    PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
            sp.edit()
                    .putString(PREF_ACCESS_TOKEN, token.getToken())
                    .putString(PREF_TOKEN_TYPE, token.getTokenType())
                    .putLong(PREF_EXPIRATION, token.getExpiration())
                    .putString(PREF_SCOPE, token.getScope())
                    .putString(PREF_REFRESH_TOKEN, token.getRefreshToken())
                    .apply();
        };
    }

    @Override
    public void clearSavedUserAccessToken() {
        mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    @Override
    public void clearSavedApplicationAccessToken() {
        mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    @Subscribe @SuppressWarnings("unused")
    public void onUserSignOut(UserSignOutEvent event) {
        AccessToken token = getSavedUserAccessToken();
        if (token != null) {
            mServiceAuth.revokeAuthToken().call(token.getToken(), "access_token");
            mServiceAuth.revokeAuthToken().call(token.getRefreshToken(), "refresh_token");
            clearSavedUserAccessToken();
            mIdentityManager.clearSavedUserIdentity();
        }
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
