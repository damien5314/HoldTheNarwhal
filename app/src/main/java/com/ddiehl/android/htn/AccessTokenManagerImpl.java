package com.ddiehl.android.htn;

import android.content.Context;
import android.content.SharedPreferences;

import rx.functions.Action1;
import rxreddit.api.AccessTokenManager;
import rxreddit.model.ApplicationAccessToken;
import rxreddit.model.UserAccessToken;

public class AccessTokenManagerImpl implements AccessTokenManager {
  private static final String PREFS_USER_ACCESS_TOKEN = "prefs_user_access_token";
  private static final String PREFS_APPLICATION_ACCESS_TOKEN = "prefs_application_access_token";
  private static final String PREF_ACCESS_TOKEN = "pref_access_token";
  private static final String PREF_TOKEN_TYPE = "pref_token_type";
  private static final String PREF_EXPIRATION = "pref_expiration";
  private static final String PREF_SCOPE = "pref_scope";
  private static final String PREF_REFRESH_TOKEN = "pref_refresh_token";

  private Context mContext = HoldTheNarwhal.getContext();

  private UserAccessToken mUserAccessToken;
  private ApplicationAccessToken mApplicationAccessToken;

  @Override
  public UserAccessToken getUserAccessToken() {
    return getSavedUserAccessToken();
  }

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

  @Override
  public ApplicationAccessToken getApplicationAccessToken() {
    return getSavedApplicationAccessToken();
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
