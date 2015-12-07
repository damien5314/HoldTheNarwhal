package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;

import rx.Observable;
import rx.functions.Action1;

public interface AccessTokenManager {
    boolean isUserAuthorized();
    boolean hasValidAccessToken();
    String getValidAccessToken();
    Observable<AccessToken> getUserAccessToken();
    Observable<AccessToken> getAccessToken();
    Action1<AccessToken> saveUserAccessToken();
    Action1<AccessToken> saveApplicationAccessToken();
    void clearSavedUserAccessToken();
    void clearSavedApplicationAccessToken();
}
