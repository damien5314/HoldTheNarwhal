package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;

import rx.Observable;

public interface AccessTokenManager {
    boolean isUserAuthorized();

    boolean hasUserAccessToken();

    boolean hasValidUserAccessToken();

    boolean hasUserAccessRefreshToken();

    boolean hasValidApplicationAccessToken();

    boolean hasValidAccessToken();

    Observable<AccessToken> getUserAccessToken();

    Observable<AccessToken> getApplicationAccessToken();

    AccessToken getSavedUserAccessToken();

    AccessToken getSavedApplicationAccessToken();

    void saveUserAccessTokenResponse(AuthorizationResponse response);

    void saveApplicationAccessTokenResponse(AuthorizationResponse response);

    void clearSavedUserAccessToken();

    void clearSavedApplicationAccessToken();
}
