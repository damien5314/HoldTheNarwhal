package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;

import rx.Observable;

public interface AccessTokenManager {
    boolean isUserAuthorized();

    boolean hasValidAccessToken();
    String getValidAccessToken();
    void onUserAuthCodeReceived(String authCode);
    Observable<AccessToken> getUserAccessToken();
    Observable<AccessToken> getApplicationAccessToken();
    AccessToken getSavedUserAccessToken();
    AccessToken getSavedApplicationAccessToken();
    void saveApplicationAccessTokenResponse(AuthorizationResponse response);
    void clearSavedUserAccessToken();
    void clearSavedApplicationAccessToken();
}
