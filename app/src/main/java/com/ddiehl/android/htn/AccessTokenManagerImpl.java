package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.UserAccessToken;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

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

  private Context mContext = HoldTheNarwhal.getContext();
  private RedditAuthService mServiceAuth = HoldTheNarwhal.getRedditServiceAuth();
  private IdentityManager mIdentityManager = HoldTheNarwhal.getIdentityManager();

  private UserAccessToken mUserAccessToken;
  private ApplicationAccessToken mApplicationAccessToken;

  @Override
  public boolean isUserAuthorized() {
    return getSavedUserAccessToken() != null;
  }

  @Override
  public AccessToken getValidAccessToken() {
    AccessToken token;
    token = getSavedUserAccessToken();
    if (token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
      return token;
    }
    token = getSavedApplicationAccessToken();
    if (token != null && token.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
      return token;
    }
    return null;
  }

  @Override
  public boolean hasValidAccessToken() {
    return getValidAccessToken() != null;
  }

  @Override
  public Observable<AccessToken> getAccessToken() {
    return getUserAccessToken()
        .map(userAccessToken -> (AccessToken) userAccessToken)
        .onErrorResumeNext(mGetApplicationAccessToken);
  }

  @Override
  public Observable<UserAccessToken> getUserAccessToken() {
    return mGetUserAccessToken;
  }

  private Func1<UserAccessToken, Observable<UserAccessToken>> refreshUserAccessToken =
      accessToken -> {
        if (accessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
          return Observable.just(accessToken);
        } else return refreshUserAccessToken(accessToken);
      };

  private Func1<ApplicationAccessToken, Observable<ApplicationAccessToken>> refreshApplicationAccessToken =
      accessToken -> {
        if (accessToken != null
            && accessToken.secondsUntilExpiration() > EXPIRATION_THRESHOLD) {
          return Observable.just(accessToken);
        } else {
          return mServiceAuth.authorizeApplication()
              .doOnNext(saveApplicationAccessToken());
        }
      };

  private Observable<UserAccessToken> mGetUserAccessToken =
      Observable.create(subscriber -> {
        UserAccessToken token = getSavedUserAccessToken();
        if (token == null) {
          subscriber.onError(new RuntimeException("No user access token available"));
        } else {
          refreshUserAccessToken.call(token)
              .subscribe(subscriber::onNext, subscriber::onError, subscriber::onCompleted);
        }
      });

  private Observable<ApplicationAccessToken> mGetApplicationAccessToken =
      Observable.create(subscriber -> {
        ApplicationAccessToken token = getSavedApplicationAccessToken();
        refreshApplicationAccessToken.call(token)
            .subscribe(subscriber::onNext, subscriber::onError, subscriber::onCompleted);
      });

  private Observable<UserAccessToken> refreshUserAccessToken(AccessToken accessToken) {
    String refreshToken = accessToken.getRefreshToken();
    if (refreshToken == null) {
      mClearIdentity.call(null);
      return Observable.error(new RuntimeException("No refresh token available"));
    }
    return mServiceAuth.refreshUserAccessToken(refreshToken)
        .doOnNext(saveUserAccessToken())
        .doOnError(mClearIdentity);
  }

  private Action1<Throwable> mClearIdentity = error -> {
    clearSavedUserAccessToken();
    mIdentityManager.clearSavedUserIdentity();
  };

  private UserAccessToken getSavedUserAccessToken() {
    if (mUserAccessToken != null) return mUserAccessToken;
    SharedPreferences sp =  mContext.getSharedPreferences(
        PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE);
    if (!sp.contains(PREF_ACCESS_TOKEN)) return null;
    UserAccessToken token = new UserAccessToken();
    token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
    token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
    token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
    token.setScope(sp.getString(PREF_SCOPE, null));
    token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
    mUserAccessToken = token;
    return token;
  }

  private ApplicationAccessToken getSavedApplicationAccessToken() {
    if (mApplicationAccessToken != null) return mApplicationAccessToken;
    SharedPreferences sp =  mContext.getSharedPreferences(
        PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE);
    if (!sp.contains(PREF_ACCESS_TOKEN)) return null;
    ApplicationAccessToken token = new ApplicationAccessToken();
    token.setToken(sp.getString(PREF_ACCESS_TOKEN, null));
    token.setTokenType(sp.getString(PREF_TOKEN_TYPE, null));
    token.setExpiration(sp.getLong(PREF_EXPIRATION, 0));
    token.setScope(sp.getString(PREF_SCOPE, null));
    token.setRefreshToken(sp.getString(PREF_REFRESH_TOKEN, null));
    mApplicationAccessToken = token;
    return token;
  }

  @Override
  public Action1<UserAccessToken> saveUserAccessToken() {
    return token -> {
      // Swap in the saved refresh token if we didn't get a new one
      if (token.getRefreshToken() == null && mUserAccessToken != null) {
        token.setRefreshToken(mUserAccessToken.getRefreshToken());
      }
      Timber.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
          token.getToken(), token.getRefreshToken()));
      mUserAccessToken = token;
      mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE).edit()
          .putString(PREF_ACCESS_TOKEN, token.getToken())
          .putString(PREF_REFRESH_TOKEN, token.getRefreshToken())
          .putString(PREF_TOKEN_TYPE, token.getTokenType())
          .putLong(PREF_EXPIRATION, token.getExpiration())
          .putString(PREF_SCOPE, token.getScope())
          .apply();
    };
  }

  @Override
  public Action1<ApplicationAccessToken> saveApplicationAccessToken() {
    return token -> {
      Timber.d(String.format("--ACCESS TOKEN RESPONSE--\nAccess Token: %s\nRefresh Token: %s",
          token.getToken(), token.getRefreshToken()));
      mApplicationAccessToken = token;
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
    AccessToken token = getSavedUserAccessToken();
    if (token != null) {
      mServiceAuth.revokeAuthToken(token)
          .subscribe(r -> {
          }, e -> {
          });
    }
    mContext.getSharedPreferences(PREFS_USER_ACCESS_TOKEN, Context.MODE_PRIVATE)
        .edit().clear().apply();
    mUserAccessToken = null;
  }

  @Override
  public void clearSavedApplicationAccessToken() {
    mContext.getSharedPreferences(PREFS_APPLICATION_ACCESS_TOKEN, Context.MODE_PRIVATE)
        .edit().clear().apply();
    mApplicationAccessToken = null;
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
