package com.ddiehl.android.htn;

import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;

public interface AccessTokenManager {
    boolean isUserAuthorized();

    boolean hasUserAccessToken();

    boolean hasValidUserAccessToken();

    boolean hasUserAccessRefreshToken();

    boolean hasValidApplicationAccessToken();

    boolean hasValidAccessToken();

    AccessToken getUserAccessToken();

    AccessToken getApplicationAccessToken();

    void saveUserAccessTokenResponse(AuthorizationResponse response);

    void saveApplicationAccessTokenResponse(AuthorizationResponse response);

    void clearSavedUserAccessToken();

    void clearSavedApplicationAccessToken();
}
