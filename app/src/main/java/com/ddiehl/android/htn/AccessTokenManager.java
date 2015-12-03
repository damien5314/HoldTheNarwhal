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
    void onUserAuthCodeReceived(String authCode);
    Observable<AccessToken> getUserAccessToken();
    Observable<AccessToken> getApplicationAccessToken();
    AccessToken getSavedUserAccessToken();
    AccessToken getSavedApplicationAccessToken();
//    Action1<AccessToken> saveUserAccessToken;
    void saveApplicationAccessTokenResponse(AuthorizationResponse response);
    void clearSavedUserAccessToken();
    void clearSavedApplicationAccessToken();
}
