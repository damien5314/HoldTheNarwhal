package com.ddiehl.android.simpleredditreader.io;

import com.ddiehl.android.simpleredditreader.model.identity.AuthTokenResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface RedditAuthAPI {

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getApplicationAuthToken(@Field("grant_type") String grantType,
                                 @Field("device_id") String deviceId,
                                 Callback<AuthTokenResponse> callback);

    @FormUrlEncoded @POST("/api/v1/access_token")
    void getUserAuthToken(@Field("grant_type") String grantType,
                          @Field("code") String code,
                          @Field("redirect_uri") String redirectUri,
                          Callback<AuthTokenResponse> callback);

    @FormUrlEncoded @POST("/api/v1/access_token")
    void refreshUserAuthToken(@Field("grant_type") String grantType,
                              @Field("refresh_token") String refreshToken,
                              Callback<AuthTokenResponse> callback);

}
