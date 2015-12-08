package com.ddiehl.android.htn.io;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.utils.BaseUtils;
import com.ddiehl.reddit.identity.AccessToken;
import com.ddiehl.reddit.identity.AuthorizationResponse;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.ResponseBody;

import rx.Observable;

public interface RedditAuthService {
    String ENDPOINT_NORMAL = "https://www.reddit.com";
    String CLIENT_ID = BuildConfig.REDDIT_APP_ID;
    String RESPONSE_TYPE = "code";
    String DURATION = "permanent";
    String STATE = BaseUtils.getRandomString();
    String REDIRECT_URI = "http://127.0.0.1/";
    String SCOPE = "identity,mysubreddits,privatemessages,read,report,save," +
            "submit,vote,history,account,subscribe";
    String HTTP_AUTH_HEADER = Credentials.basic(CLIENT_ID, "");
    String AUTHORIZATION_URL =
                    String.format("https://www.reddit.com/api/v1/authorize.compact?client_id=%s" +
                                    "&response_type=%s&duration=%s&state=%s&redirect_uri=%s&scope=%s",
                            CLIENT_ID, RESPONSE_TYPE, DURATION, STATE, REDIRECT_URI, SCOPE);

    Observable<AuthorizationResponse> authorizeApplication();

    Observable<AuthorizationResponse> getUserAccessToken(
            String grantType, String authCode, String redirectUri);

    Observable<AuthorizationResponse> refreshUserAccessToken(String refreshToken);

    Observable<ResponseBody> revokeAuthToken(AccessToken token);
}
