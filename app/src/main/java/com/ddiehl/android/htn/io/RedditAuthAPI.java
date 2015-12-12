package com.ddiehl.android.htn.io;


import com.ddiehl.reddit.identity.ApplicationAccessToken;
import com.ddiehl.reddit.identity.UserAccessToken;
import com.squareup.okhttp.ResponseBody;

import retrofit.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface RedditAuthAPI {

  @FormUrlEncoded @POST("/api/v1/access_token")
  Observable<Response<ApplicationAccessToken>> getApplicationAuthToken(
      @Field("grant_type") String grantType,
      @Field("device_id") String deviceId);

  @FormUrlEncoded @POST("/api/v1/access_token")
  Observable<Response<UserAccessToken>> getUserAuthToken(
      @Field("grant_type") String grantType,
      @Field("code") String code,
      @Field("redirect_uri") String redirectUri);

  @FormUrlEncoded @POST("/api/v1/access_token")
  Observable<Response<UserAccessToken>> refreshUserAuthToken(
      @Field("grant_type") String grantType,
      @Field("refresh_token") String refreshToken);

  @FormUrlEncoded @POST("/api/v1/revoke_token")
  Observable<Response<ResponseBody>> revokeUserAuthToken(
      @Field("token") String token,
      @Field("token_type_hint") String tokenTypeHint);

}
