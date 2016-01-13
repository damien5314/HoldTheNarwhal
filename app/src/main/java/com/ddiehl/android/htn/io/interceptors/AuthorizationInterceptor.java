package com.ddiehl.android.htn.io.interceptors;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.io.RedditAuthService;
import com.ddiehl.reddit.identity.AccessToken;

import okhttp3.Interceptor;
import okhttp3.Request;

public class AuthorizationInterceptor {
  public enum Type { HTTP_AUTH, TOKEN_AUTH }

  private AuthorizationInterceptor() { }

  public static Interceptor get(Type t) {
    switch (t) {
      case HTTP_AUTH:
        return (chain) -> {
          Request originalRequest = chain.request();
          Request newRequest = originalRequest.newBuilder()
              .removeHeader("Authorization")
              .addHeader("Authorization", RedditAuthService.HTTP_AUTH_HEADER)
              .build();
          return chain.proceed(newRequest);
        };
      case TOKEN_AUTH:
        return chain -> {
          Request originalRequest = chain.request();
          AccessToken token = HoldTheNarwhal.getAccessTokenManager().getValidAccessToken();
          HoldTheNarwhal.getLogger()
              .d("Access token expires in " + token.secondsUntilExpiration() + " seconds");
          Request newRequest = originalRequest.newBuilder()
              .removeHeader("Authorization")
              .addHeader("Authorization", "bearer " + token.getToken())
              .build();
          return chain.proceed(newRequest);
        };
      default:
        /* no-op */
        return (chain) -> chain.proceed(chain.request());
    }
  }
}
