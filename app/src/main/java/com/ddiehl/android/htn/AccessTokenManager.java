package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;

import rx.Observable;

public interface AccessTokenManager {
    boolean isUserAuthorized();

    boolean hasValidAccessToken();
    String getValidAccessToken();
    void onUserAuthCodeReceived(String authCode);
    Observable<AccessToken> getUserAccessToken();
    Observable<AccessToken> getAccessToken();
    AccessToken getSavedUserAccessToken();
    AccessToken getSavedApplicationAccessToken();
    void clearSavedUserAccessToken();
    void clearSavedApplicationAccessToken();
}
