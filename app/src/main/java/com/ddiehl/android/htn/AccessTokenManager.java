package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.UserAccessToken;

import rx.Observable;
import rx.functions.Action1;

public interface AccessTokenManager {
  boolean isUserAuthorized();
  boolean hasValidAccessToken();
  AccessToken getValidAccessToken();
  Observable<UserAccessToken> getUserAccessToken();
  Observable<AccessToken> getAccessToken();
  Action1<UserAccessToken> saveUserAccessToken();
  Action1<ApplicationAccessToken> saveApplicationAccessToken();
  void clearSavedUserAccessToken();
  void clearSavedApplicationAccessToken();
}
