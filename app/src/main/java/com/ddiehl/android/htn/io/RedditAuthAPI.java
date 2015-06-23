package com.ddiehl.android.htn.io;


import com.ddiehl.reddit.identity.AuthorizationResponse;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface RedditAuthAPI {

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getApplicationAuthToken(@Field("grant_type") String grantType,
                                 @Field("device_id") String deviceId,
                                 Callback<AuthorizationResponse> callback);

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getUserAuthToken(@Field("grant_type") String grantType,
                          @Field("code") String code,
                          @Field("redirect_uri") String redirectUri,
                          Callback<AuthorizationResponse> callback);

    @FormUrlEncoded @POST("/api/v1/access_token")
    void refreshUserAuthToken(@Field("grant_type") String grantType,
                              @Field("refresh_token") String refreshToken,
                              Callback<AuthorizationResponse> callback);

    @FormUrlEncoded @POST("/api/v1/revoke_token")
    void revokeUserAuthToken(@Field("token") String token,
                             @Field("token_type_hint") String tokenTypeHint,
                             Callback<Response> callback);

}
