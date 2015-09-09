/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.io;


import com.ddiehl.reddit.identity.AuthorizationResponse;

import retrofit.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface RedditAuthAPI {

    @FormUrlEncoded @POST("/api/v1/access_token")
    Observable<Response<AuthorizationResponse>> getApplicationAuthToken(
            @Field("grant_type") String grantType,
            @Field("device_id") String deviceId);

    @FormUrlEncoded @POST("/api/v1/access_token")
    Observable<Response<AuthorizationResponse>> getUserAuthToken(
            @Field("grant_type") String grantType,
            @Field("code") String code,
            @Field("redirect_uri") String redirectUri);

    @FormUrlEncoded @POST("/api/v1/access_token")
    Observable<Response<AuthorizationResponse>> refreshUserAuthToken(
            @Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken);

    @FormUrlEncoded @POST("/api/v1/revoke_token")
    Observable<Response<String>> revokeUserAuthToken(
            @Field("token") String token,
            @Field("token_type_hint") String tokenTypeHint);

}
